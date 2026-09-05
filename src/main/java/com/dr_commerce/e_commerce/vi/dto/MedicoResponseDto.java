package com.dr_commerce.e_commerce.vi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MedicoResponseDto {

    private final Long id;
    private final String nombre;
    private final String apellido;
    private final String matricula;
    private final String especialidad;
}
