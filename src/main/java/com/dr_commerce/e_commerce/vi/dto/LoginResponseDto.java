package com.dr_commerce.e_commerce.vi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDto {

    private final String token;
    private final UsuarioResponseDto usuario;
}
