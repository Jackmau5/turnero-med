package com.dr_commerce.e_commerce.vi.dto;

import com.dr_commerce.e_commerce.vi.model.EstadoCarrito;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class CarritoResponseDto {

    private final Long id;
    private final Long pacienteId;
    private final EstadoCarrito estado;
    private final LocalDateTime fechaCreacion;
    private final List<TurnoResponseDto> turnos;
}
