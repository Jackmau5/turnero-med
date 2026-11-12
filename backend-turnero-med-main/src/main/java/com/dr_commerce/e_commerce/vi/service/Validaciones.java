package com.dr_commerce.e_commerce.vi.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

// Validaciones sencillas compartidas por los servicios.
public final class Validaciones {
    private Validaciones() {}
    public static String texto(String valor, String campo) {
        if (valor == null || valor.isBlank() || valor.trim().length() > 255)
            throw new IllegalArgumentException(campo + " es obligatorio y debe tener hasta 255 caracteres");
        return valor.trim();
    }
    public static String email(String valor) {
        String email = texto(valor, "El email").toLowerCase(Locale.ROOT);
        if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
            throw new IllegalArgumentException("El email no tiene un formato válido");
        return email;
    }
    public static String password(String valor) {
        if (valor == null || valor.isBlank() || valor.length() < 8 || valor.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres y hasta 72 bytes");
        return valor;
    }
    public static String dni(String valor) {
        String dni = texto(valor, "El DNI");
        if (!dni.matches("[0-9]{7,10}")) throw new IllegalArgumentException("El DNI debe tener entre 7 y 10 dígitos");
        return dni;
    }
}
