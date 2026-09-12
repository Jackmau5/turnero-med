package com.dr_commerce.e_commerce.vi.config;

import com.dr_commerce.e_commerce.vi.model.Role;
import com.dr_commerce.e_commerce.vi.model.Usuario;
import com.dr_commerce.e_commerce.vi.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Bootstrap para desarrollo y para esta entrega académica: como /api/auth/register
 * ahora requiere rol ADMIN, hace falta que exista al menos un admin desde el arranque.
 * Si todavía no hay ninguno, se crea uno con las credenciales de application.properties.
 * No es un mecanismo pensado para un entorno de producción real.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminSeeder(UsuarioRepository usuarioRepository,
                        PasswordEncoder passwordEncoder,
                        @Value("${app.admin.email}") String adminEmail,
                        @Value("${app.admin.password}") String adminPassword) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        boolean yaExisteAdmin = usuarioRepository.findAll().stream()
                .anyMatch(usuario -> usuario.getRole() == Role.ADMIN);
        if (yaExisteAdmin) {
            return;
        }

        Usuario admin = new Usuario();
        admin.setNombre("Admin");
        admin.setApellido("Turnero");
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        usuarioRepository.save(admin);
    }
}
