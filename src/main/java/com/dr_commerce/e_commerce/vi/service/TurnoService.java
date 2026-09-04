package com.dr_commerce.e_commerce.vi.service;

import com.dr_commerce.e_commerce.vi.dto.TurnoRequestDto;
import com.dr_commerce.e_commerce.vi.dto.TurnoResponseDto;
import com.dr_commerce.e_commerce.vi.exception.TurnoNotFoundException;
import com.dr_commerce.e_commerce.vi.model.Turno;
import com.dr_commerce.e_commerce.vi.repository.TurnoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TurnoService {

    private final TurnoRepository turnoRepository;

    public TurnoService(TurnoRepository turnoRepository) {
        this.turnoRepository = turnoRepository;
    }

    public List<TurnoResponseDto> obtenerTodos() {
        return turnoRepository.findAll().stream().map(this::aResponseDto).toList();
    }

    public TurnoResponseDto obtenerPorId(Long id) {
        return turnoRepository.findById(id)
                .map(this::aResponseDto)
                .orElseThrow(() -> new TurnoNotFoundException(id));
    }

    @Transactional
    public TurnoResponseDto crear(TurnoRequestDto request) {
        Turno turno = new Turno();
        completarTurno(turno, request);
        return aResponseDto(turnoRepository.save(turno));
    }

    @Transactional
    public TurnoResponseDto actualizar(Long id, TurnoRequestDto request) {
        Turno turno = turnoRepository.findById(id)
                .orElseThrow(() -> new TurnoNotFoundException(id));
        completarTurno(turno, request);
        return aResponseDto(turnoRepository.save(turno));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!turnoRepository.existsById(id)) {
            throw new TurnoNotFoundException(id);
        }
        turnoRepository.deleteById(id);
    }

    private void completarTurno(Turno turno, TurnoRequestDto request) {
        turno.setPaciente(request.getPaciente());
        turno.setMedico(request.getMedico());
        turno.setEspecialidad(request.getEspecialidad());
        turno.setFecha(request.getFecha());
        turno.setDireccion(request.getDireccion());
        turno.setEstado(request.getEstado());
    }

    private TurnoResponseDto aResponseDto(Turno turno) {
        return new TurnoResponseDto(
                turno.getId(),
                turno.getPaciente(),
                turno.getMedico(),
                turno.getEspecialidad(),
                turno.getFecha(),
                turno.getDireccion(),
                turno.getEstado()
        );
    }
}
