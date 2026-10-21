package com.dr_commerce.e_commerce.vi.dto;

import com.dr_commerce.e_commerce.vi.model.Rol;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UsuarioAddRequestDto {

    private String nombre;
    private String apellido;
    private String email;
    private String password;
    // Opcional: si no se envía, se asigna PACIENTE por defecto en AuthService
    private Rol rol;
}