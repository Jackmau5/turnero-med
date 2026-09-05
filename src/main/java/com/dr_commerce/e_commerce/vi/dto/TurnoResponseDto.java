package com.dr_commerce.e_commerce.vi.dto;

import com.dr_commerce.e_commerce.vi.model.Direccion;
import com.dr_commerce.e_commerce.vi.model.Fecha;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TurnoResponseDto {

    private final Long id;
    private final Long pacienteId;
    private final Long medicoId;
    private final Fecha fecha;
    private final Direccion direccion;
    private final String estado;
}
