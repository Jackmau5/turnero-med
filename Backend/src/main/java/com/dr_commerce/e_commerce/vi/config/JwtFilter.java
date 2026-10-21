package com.dr_commerce.e_commerce.vi.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Leer el header Authorization
        final String authHeader = request.getHeader("Authorization");

        // 2. Si no hay token o no empieza con "Bearer ", continuar sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraer el token (quitar "Bearer ")
        final String token = authHeader.substring(7);

        // 4. Extraer el email del token
        final String email = jwtUtil.extraerEmail(token);

        // 5. Si hay email y el contexto de seguridad aún no tiene autenticación
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 6. Cargar el usuario desde la base de datos
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // 7. Validar el token
            if (jwtUtil.validarToken(token, userDetails)) {

                // 8. Crear el objeto de autenticación y setearlo en el contexto
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 9. Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}
