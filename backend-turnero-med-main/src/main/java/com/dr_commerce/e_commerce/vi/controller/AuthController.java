package com.dr_commerce.e_commerce.vi.controller;

import com.dr_commerce.e_commerce.vi.config.JwtFilter;
import com.dr_commerce.e_commerce.vi.config.JwtUtil;
import com.dr_commerce.e_commerce.vi.dto.UsuarioAddRequestDto;
import com.dr_commerce.e_commerce.vi.dto.UsuarioRequestDto;
import com.dr_commerce.e_commerce.vi.dto.UsuarioResponseDto;
import com.dr_commerce.e_commerce.vi.model.Usuario;
import com.dr_commerce.e_commerce.vi.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwt;
    private final CsrfTokenRepository csrfTokens;
    private final boolean cookieSecure;

    public AuthController(AuthService authService, JwtUtil jwt, CsrfTokenRepository csrfTokens,
                          @Value("${app.auth.cookie-secure:true}") boolean cookieSecure) {
        this.authService = authService;
        this.jwt = jwt;
        this.csrfTokens = csrfTokens;
        this.cookieSecure = cookieSecure;
    }

    @GetMapping("/csrf")
    public ResponseEntity<Map<String, String>> csrf(CsrfToken csrfToken) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(Map.of("token", csrfToken.getToken()));
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDto> register(@RequestBody UsuarioAddRequestDto request,
            HttpServletRequest httpRequest, HttpServletResponse response) {
        return iniciarSesion(authService.register(request), HttpStatus.CREATED, httpRequest, response);
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponseDto> login(@RequestBody UsuarioRequestDto request,
            HttpServletRequest httpRequest, HttpServletResponse response) {
        return iniciarSesion(authService.login(request), HttpStatus.OK, httpRequest, response);
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDto> me(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(perfil(usuario));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        csrfTokens.saveToken(null, request, response);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString())
                .build();
    }

    private ResponseEntity<UsuarioResponseDto> iniciarSesion(Usuario usuario, HttpStatus status,
            HttpServletRequest request, HttpServletResponse response) {
        csrfTokens.saveToken(null, request, response);
        return ResponseEntity.status(status).cacheControl(CacheControl.noStore())
                .header(HttpHeaders.SET_COOKIE, cookie(jwt.generarToken(usuario), JwtUtil.VIGENCIA).toString())
                .body(perfil(usuario));
    }

    private ResponseCookie cookie(String valor, Duration vigencia) {
        return ResponseCookie.from(JwtFilter.COOKIE_NAME, valor)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/api")
                .sameSite("Lax")
                .maxAge(vigencia)
                .build();
    }

    private UsuarioResponseDto perfil(Usuario usuario) {
        return new UsuarioResponseDto(usuario.getId(), usuario.getNombre(), usuario.getApellido(),
                usuario.getEmail(), usuario.getRol());
    }
}
