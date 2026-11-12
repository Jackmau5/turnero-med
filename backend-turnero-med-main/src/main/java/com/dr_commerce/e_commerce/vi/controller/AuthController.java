package com.dr_commerce.e_commerce.vi.controller;

import com.dr_commerce.e_commerce.vi.config.CookieSesion;
import com.dr_commerce.e_commerce.vi.dto.UsuarioAddRequestDto;
import com.dr_commerce.e_commerce.vi.dto.UsuarioRequestDto;
import com.dr_commerce.e_commerce.vi.dto.UsuarioResponseDto;
import com.dr_commerce.e_commerce.vi.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CookieSesion cookieSesion;

    public AuthController(AuthService authService, CookieSesion cookieSesion) {
        this.authService = authService;
        this.cookieSesion = cookieSesion;
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDto> register(@RequestBody UsuarioAddRequestDto request) {
        UsuarioResponseDto usuarioCreado = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookieSesion.crear(usuarioCreado.getToken()).toString())
                .body(usuarioCreado);
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponseDto> login(@RequestBody UsuarioRequestDto request) {
        UsuarioResponseDto usuario = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieSesion.crear(usuario.getToken()).toString())
                .body(usuario);
    }

    /** Cierra la sesión: el servidor es quien borra la cookie HttpOnly. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieSesion.borrar().toString())
                .build();
    }

    /** Devuelve el usuario de la sesión actual (el front lo usa al cargar la página). */
    @GetMapping("/me")
    public UsuarioResponseDto me(Authentication authentication) {
        return authService.actual(authentication.getName());
    }
}
