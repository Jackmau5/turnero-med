package com.dr_commerce.e_commerce.vi.dto;

import com.dr_commerce.e_commerce.vi.model.Direccion;
import com.dr_commerce.e_commerce.vi.model.Fecha;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TurnoRequestDto {

    private Long pacienteId;
    private Long medicoId;
    private Fecha fecha;
    private Direccion direccion;
    private String estado;
}
