package com.dr_commerce.e_commerce.vi.service;

import com.dr_commerce.e_commerce.vi.dto.LoginResponseDto;
import com.dr_commerce.e_commerce.vi.dto.UsuarioAddRequestDto;
import com.dr_commerce.e_commerce.vi.dto.UsuarioRequestDto;
import com.dr_commerce.e_commerce.vi.dto.UsuarioResponseDto;
import com.dr_commerce.e_commerce.vi.model.Usuario;
import com.dr_commerce.e_commerce.vi.repository.UsuarioRepository;
import com.dr_commerce.e_commerce.vi.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UsuarioResponseDto register(UsuarioAddRequestDto request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con ese email");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));

        Usuario guardado = usuarioRepository.save(usuario);

        return aResponseDto(guardado);
    }

    @Transactional(readOnly = true)
    public LoginResponseDto login(UsuarioRequestDto request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new IllegalArgumentException("Email o contraseña incorrectos");
        }

        String token = jwtService.generarToken(usuario);
        return new LoginResponseDto(token, aResponseDto(usuario));
    }

    private UsuarioResponseDto aResponseDto(Usuario usuario) {
        return new UsuarioResponseDto(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getRole()
        );
    }
}