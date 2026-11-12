package com.dr_commerce.e_commerce.vi.dto;

import com.dr_commerce.e_commerce.vi.model.Especialidad;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MedicoResponseDto {

    private final Long id;
    private final String nombre;
    private final String apellido;
    private final String email;
    private final String matricula;
    private final Especialidad especialidad;
}
