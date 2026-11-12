package com.dr_commerce.e_commerce.vi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UsuarioResponseDto {

    private final Long id;
    private final String nombre;
    private final String apellido;
    private final String email;
    private final com.dr_commerce.e_commerce.vi.model.Rol rol;
    private final String token;  // JWT generado en login/register
}
