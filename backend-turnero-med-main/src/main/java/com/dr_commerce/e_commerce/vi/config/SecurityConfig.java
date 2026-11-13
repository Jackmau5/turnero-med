package com.dr_commerce.e_commerce.vi.config;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtFilter jwtFilter;
    public SecurityConfig(JwtFilter jwtFilter) { this.jwtFilter = jwtFilter; }
    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean
    public CsrfTokenRepository csrfTokenRepository(
            @Value("${app.auth.cookie-secure:true}") boolean cookieSecure) {
        var repository = new CookieCsrfTokenRepository();
        repository.setCookieName("turnero-csrf");
        repository.setCookieCustomizer(cookie -> cookie.httpOnly(true).secure(cookieSecure)
                .path("/api").sameSite("Lax"));
        return repository;
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CsrfTokenRepository csrfTokens) throws Exception {
        return http
            .csrf(csrf -> csrf.csrfTokenRepository(csrfTokens))
            .cors(cors -> {})
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) -> {
                    res.setStatus(401); res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"status\":401,\"mensaje\":\"Iniciá sesión para continuar\"}");
                })
                .accessDeniedHandler((req, res, ex) -> {
                    res.setStatus(403); res.setContentType("application/json;charset=UTF-8");
                    if (ex instanceof CsrfException) {
                        res.getWriter().write("{\"status\":403,\"codigo\":\"CSRF_INVALIDO\",\"mensaje\":\"No se pudo validar la seguridad de la solicitud\"}");
                    } else {
                        res.getWriter().write("{\"status\":403,\"mensaje\":\"No tenés permiso para realizar esta acción\"}");
                    }
                }))
            .authorizeHttpRequests(a -> a
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login", "/api/auth/logout").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
                .requestMatchers("/error", "/actuator/health").permitAll()
                .requestMatchers("/api/turnos/mis-turnos").hasRole("PACIENTE")
                .requestMatchers(HttpMethod.GET, "/api/medicos/**", "/api/turnos/**").permitAll()
                .requestMatchers("/api/medicos/**").hasRole("ADMIN")
                .requestMatchers("/api/carritos/**").hasRole("PACIENTE")
                .anyRequest().authenticated())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.origins}") List<String> origins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
