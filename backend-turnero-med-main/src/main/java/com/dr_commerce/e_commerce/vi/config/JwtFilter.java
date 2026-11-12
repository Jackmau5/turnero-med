package com.dr_commerce.e_commerce.vi.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
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
    private final CookieSesion cookieSesion;

    public JwtFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService, CookieSesion cookieSesion) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.cookieSesion = cookieSesion;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Buscar el token: primero el header Authorization (Postman, tests) y si no, la cookie HttpOnly
        String token = null;
        boolean desdeCookie = false;
        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            token = CookieSesion.leer(request);
            desdeCookie = token != null;
        }

        // 2. Sin token: continuar sin autenticar
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 3. Extraer el email del token
            final String email = jwtUtil.extraerEmail(token);

            // 4. Si hay email y el contexto de seguridad aún no tiene autenticación
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 5. Cargar el usuario desde la base de datos
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // 6. Validar el token
                if (jwtUtil.validarToken(token, userDetails)) {

                    // 7. Crear el objeto de autenticación y setearlo en el contexto
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

        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException |
                 org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            SecurityContextHolder.clearContext();
            if (desdeCookie) {
                // Cookie vencida o inválida: se borra y la petición sigue como anónima.
                // Así los endpoints públicos (y el login) no quedan bloqueados por una cookie vieja.
                response.addHeader(HttpHeaders.SET_COOKIE, cookieSesion.borrar().toString());
                filterChain.doFilter(request, response);
                return;
            }
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\":401,\"mensaje\":\"Token inválido o vencido. Iniciá sesión nuevamente\"}");
            return;
        }
        // 8. Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}
