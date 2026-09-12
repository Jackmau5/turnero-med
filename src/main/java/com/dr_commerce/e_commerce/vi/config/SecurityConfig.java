package com.dr_commerce.e_commerce.vi.config;

import com.dr_commerce.e_commerce.vi.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login").permitAll()

                // El registro de un Usuario "pelado" (sin ser Paciente ni Medico) equivale
                // a crear una cuenta ADMIN, así que no puede quedar abierto a cualquiera.
                .requestMatchers("/api/auth/register").hasRole("ADMIN")

                // Ver médicos y turnos disponibles no requiere estar logueado (como "ver productos" del e-commerce de referencia)
                .requestMatchers(HttpMethod.GET, "/api/medicos/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/turnos/**").permitAll()

                // Alta de paciente = registro de paciente, tiene que quedar público
                .requestMatchers(HttpMethod.POST, "/api/pacientes/**").permitAll()

                // Gestión del staff médico: solo un admin da de alta/edita/borra médicos
                .requestMatchers(HttpMethod.POST, "/api/medicos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/medicos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/medicos/**").hasRole("ADMIN")

                // Publicar/editar/borrar turnos es tarea del médico (o de un admin)
                .requestMatchers(HttpMethod.POST, "/api/turnos/**").hasAnyRole("MEDICO", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/turnos/**").hasAnyRole("MEDICO", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/turnos/**").hasAnyRole("MEDICO", "ADMIN")

                // Ver/editar pacientes requiere estar logueado; borrar un paciente queda para admin
                .requestMatchers(HttpMethod.GET, "/api/pacientes/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/pacientes/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/pacientes/**").hasRole("ADMIN")

                // El carrito de turnos es una acción del propio paciente
                .requestMatchers("/api/carritos/**").hasAnyRole("PACIENTE", "ADMIN")

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
