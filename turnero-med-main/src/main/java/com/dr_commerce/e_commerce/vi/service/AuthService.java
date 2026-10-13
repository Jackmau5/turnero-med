package com.dr_commerce.e_commerce.vi.service;

import com.dr_commerce.e_commerce.vi.config.JwtUtil;
import com.dr_commerce.e_commerce.vi.dto.*;
import com.dr_commerce.e_commerce.vi.model.*;
import com.dr_commerce.e_commerce.vi.repository.*;
import com.dr_commerce.e_commerce.vi.exception.ConflictoException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PacienteRepository pacientes;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;
    public AuthService(UsuarioRepository usuarios, PacienteRepository pacientes, PasswordEncoder encoder, JwtUtil jwt) {
        this.usuarios = usuarios; this.pacientes = pacientes; this.encoder = encoder; this.jwt = jwt;
    }
    @Transactional
    public UsuarioResponseDto register(UsuarioAddRequestDto request) {
        if (request.getRol() != null && request.getRol() != Rol.PACIENTE)
            throw new IllegalArgumentException("El registro público solo permite pacientes");
        String email = Validaciones.email(request.getEmail());
        String dni = Validaciones.dni(request.getDni());
        if (usuarios.existsByEmail(email)) throw new ConflictoException("Ya existe un usuario con ese email");
        if (pacientes.existsByDni(dni)) throw new ConflictoException("Ya existe un paciente con ese DNI");
        Paciente paciente = new Paciente();
        paciente.setNombre(Validaciones.texto(request.getNombre(), "El nombre"));
        paciente.setApellido(Validaciones.texto(request.getApellido(), "El apellido"));
        paciente.setEmail(email);
        paciente.setDni(dni);
        paciente.setPassword(encoder.encode(Validaciones.password(request.getPassword())));
        paciente.setRol(Rol.PACIENTE);
        return respuesta(pacientes.save(paciente));
    }
    @Transactional(readOnly = true)
    public UsuarioResponseDto login(UsuarioRequestDto request) {
        String email = Validaciones.email(request.getEmail());
        if (request.getPassword() == null || request.getPassword().isBlank())
            throw new IllegalArgumentException("La contraseña es obligatoria");
        Usuario usuario = usuarios.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
        if (!encoder.matches(request.getPassword(), usuario.getPassword()))
            throw new BadCredentialsException("Credenciales inválidas");
        if (!usuario.isEnabled()) throw new DisabledException("Cuenta desactivada");
        return respuesta(usuario);
    }
    private UsuarioResponseDto respuesta(Usuario usuario) {
        return new UsuarioResponseDto(usuario.getId(), usuario.getNombre(), usuario.getApellido(),
                usuario.getEmail(), usuario.getRol(), jwt.generarToken(usuario));
    }
}
