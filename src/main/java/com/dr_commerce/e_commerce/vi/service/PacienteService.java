package com.dr_commerce.e_commerce.vi.service;

import com.dr_commerce.e_commerce.vi.dto.PacienteRequestDto;
import com.dr_commerce.e_commerce.vi.dto.PacienteResponseDto;
import com.dr_commerce.e_commerce.vi.exception.PacienteNotFoundException;
import com.dr_commerce.e_commerce.vi.model.Paciente;
import com.dr_commerce.e_commerce.vi.repository.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    public List<PacienteResponseDto> obtenerTodos() {
        return pacienteRepository.findAll().stream().map(this::aResponseDto).toList();
    }

    public PacienteResponseDto obtenerPorId(Long id) {
        return pacienteRepository.findById(id)
                .map(this::aResponseDto)
                .orElseThrow(() -> new PacienteNotFoundException(id));
    }

    @Transactional
    public PacienteResponseDto crear(PacienteRequestDto request) {
        if (pacienteRepository.existsByDni(request.getDni())) {
            throw new IllegalArgumentException("Ya existe un paciente registrado con ese DNI");
        }
        Paciente paciente = new Paciente();
        completarPaciente(paciente, request);
        return aResponseDto(pacienteRepository.save(paciente));
    }

    @Transactional
    public PacienteResponseDto actualizar(Long id, PacienteRequestDto request) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new PacienteNotFoundException(id));
        completarPaciente(paciente, request);
        return aResponseDto(pacienteRepository.save(paciente));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!pacienteRepository.existsById(id)) {
            throw new PacienteNotFoundException(id);
        }
        pacienteRepository.deleteById(id);
    }

    private void completarPaciente(Paciente paciente, PacienteRequestDto request) {
        paciente.setNombre(request.getNombre());
        paciente.setApellido(request.getApellido());
        paciente.setDni(request.getDni());
    }

    private PacienteResponseDto aResponseDto(Paciente paciente) {
        return new PacienteResponseDto(
                paciente.getId(),
                paciente.getNombre(),
                paciente.getApellido(),
                paciente.getDni()
        );
    }
}
