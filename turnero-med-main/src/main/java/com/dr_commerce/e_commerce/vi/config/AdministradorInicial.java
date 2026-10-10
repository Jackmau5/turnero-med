package com.dr_commerce.e_commerce.vi.config;

import com.dr_commerce.e_commerce.vi.model.*;
import com.dr_commerce.e_commerce.vi.repository.UsuarioRepository;
import com.dr_commerce.e_commerce.vi.service.Validaciones;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("mysql")
public class AdministradorInicial implements CommandLineRunner {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final String email;
    private final String password;
    public AdministradorInicial(UsuarioRepository usuarios, PasswordEncoder encoder,
            @Value("${app.admin.email}") String email, @Value("${app.admin.password}") String password) {
        this.usuarios = usuarios; this.encoder = encoder; this.email = email; this.password = password;
    }
    @Override
    @Transactional
    public void run(String... args) {
        if (email.isBlank() || usuarios.existsByRol(Rol.ADMIN)) return;
        String normalizado = Validaciones.email(email);
        if (usuarios.existsByEmail(normalizado))
            throw new IllegalArgumentException("ADMIN_EMAIL ya pertenece a otra cuenta; elegí otro email");
        Usuario admin = new Usuario();
        admin.setNombre("Administrador"); admin.setApellido("Inicial");
        admin.setEmail(normalizado); admin.setRol(Rol.ADMIN);
        admin.setPassword(encoder.encode(Validaciones.password(password)));
        usuarios.save(admin);
    }
}
