package com.dr_commerce.e_commerce.vi.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MedicoRequestDto {

    private String nombre;
    private String apellido;
    private String matricula;
    private String especialidad;
}
