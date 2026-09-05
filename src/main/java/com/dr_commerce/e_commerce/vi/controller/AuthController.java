package com.dr_commerce.e_commerce.vi.controller;

import com.dr_commerce.e_commerce.vi.dto.UsuarioAddRequestDto;
import com.dr_commerce.e_commerce.vi.dto.UsuarioRequestDto;
import com.dr_commerce.e_commerce.vi.dto.UsuarioResponseDto;
import com.dr_commerce.e_commerce.vi.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDto> register(@RequestBody UsuarioAddRequestDto request) {
        UsuarioResponseDto usuarioCreado = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioCreado);
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponseDto> login(@RequestBody UsuarioRequestDto request) {
        UsuarioResponseDto usuario = authService.login(request);
        return ResponseEntity.ok(usuario);
    }
}
