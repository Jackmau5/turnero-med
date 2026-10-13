package com.dr_commerce.e_commerce.vi.service;

import com.dr_commerce.e_commerce.vi.dto.TurnoResponseDto;
import com.dr_commerce.e_commerce.vi.model.Turno;
import java.math.BigDecimal;

public final class DtoMapper {
    private DtoMapper() {}
    public static TurnoResponseDto turno(Turno turno) {
        return new TurnoResponseDto(turno.getId(),
                turno.getPaciente() == null ? null : turno.getPaciente().getId(),
                turno.getMedico().getId(), turno.getFecha(), turno.getDireccion(), turno.getEstado(),
                turno.getPrecio() == null ? BigDecimal.ZERO : turno.getPrecio(),
                turno.getMedico().getNombre() + " " + turno.getMedico().getApellido(),
                turno.getMedico().getEspecialidad());
    }
}
