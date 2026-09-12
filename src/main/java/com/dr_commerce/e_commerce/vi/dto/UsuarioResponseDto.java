package com.dr_commerce.e_commerce.vi.dto;

import com.dr_commerce.e_commerce.vi.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UsuarioResponseDto {

    private final Long id;
    private final String nombre;
    private final String apellido;
    private final String email;
    private final Role role;
}
