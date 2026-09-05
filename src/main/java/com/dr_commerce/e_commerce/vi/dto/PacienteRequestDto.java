package com.dr_commerce.e_commerce.vi.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PacienteRequestDto {

    private String nombre;
    private String apellido;
    private String dni;
}
