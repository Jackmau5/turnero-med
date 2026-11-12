package com.dr_commerce.e_commerce.vi.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Cookie que transporta el JWT. Es HttpOnly: JavaScript no puede leerla, por lo que un XSS
 * no puede robar el token. SameSite evita que otros sitios la envíen en POST/PUT/DELETE.
 */
@Component
public class CookieSesion {

    public static final String NOMBRE = "turnero_token";

    private final JwtUtil jwt;
    private final boolean secure;
    private final String sameSite;

    public CookieSesion(JwtUtil jwt,
                        @Value("${app.cookie.secure:false}") boolean secure,
                        @Value("${app.cookie.same-site:Lax}") String sameSite) {
        this.jwt = jwt;
        this.secure = secure;
        this.sameSite = sameSite;
    }

    /** Cookie con el token; dura lo mismo que el JWT. */
    public ResponseCookie crear(String token) {
        return base(token).maxAge(Duration.ofMillis(jwt.getExpiracionMs())).build();
    }

    /** Cookie vacía con Max-Age=0: el navegador la elimina. */
    public ResponseCookie borrar() {
        return base("").maxAge(Duration.ZERO).build();
    }

    /** Devuelve el token guardado en la cookie, o null si no viene. */
    public static String leer(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (NOMBRE.equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) return c.getValue();
        }
        return null;
    }

    private ResponseCookie.ResponseCookieBuilder base(String valor) {
        return ResponseCookie.from(NOMBRE, valor)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/api");
    }
}
