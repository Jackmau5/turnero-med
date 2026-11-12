package com.dr_commerce.e_commerce.vi.dto;

import com.dr_commerce.e_commerce.vi.model.Direccion;
import com.dr_commerce.e_commerce.vi.model.EstadoTurno;
import com.dr_commerce.e_commerce.vi.model.Fecha;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TurnoRequestDto {

    // No se puede asignar un paciente directamente: se confirma desde el carrito.
    private Long pacienteId;
    private Long medicoId;
    private Fecha fecha;
    private Direccion direccion;
    private EstadoTurno estado;
    private java.math.BigDecimal precio;
}
