package com.dr_commerce.e_commerce.vi.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // Clave secreta para firmar el token (mínimo 256 bits para HMAC-SHA256)
    private final String secret;
    public JwtUtil(@org.springframework.beans.factory.annotation.Value("${app.jwt.secret}") String secret) {
        if (secret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32)
            throw new IllegalArgumentException("JWT_SECRET debe tener al menos 32 bytes");
        this.secret = secret;
    }
    private static final long EXPIRACION_MS = 1000 * 60 * 60 * 10; // 10 horas

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    // Genera un token JWT para el usuario dado
    public String generarToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", userDetails.getAuthorities().iterator().next().getAuthority());
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRACION_MS))
                .signWith(getSecretKey())
                .compact();
    }

    // Extrae el email (subject) del token
    public String extraerEmail(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    // Valida que el token corresponda al usuario y no esté vencido
    public boolean validarToken(String token, UserDetails userDetails) {
        final String email = extraerEmail(token);
        return email.equals(userDetails.getUsername()) && !estaVencido(token);
    }

    private boolean estaVencido(String token) {
        return extraerClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extraerClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }
}
