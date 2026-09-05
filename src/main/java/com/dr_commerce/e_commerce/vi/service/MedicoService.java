package com.dr_commerce.e_commerce.vi.service;

import com.dr_commerce.e_commerce.vi.dto.MedicoRequestDto;
import com.dr_commerce.e_commerce.vi.dto.MedicoResponseDto;
import com.dr_commerce.e_commerce.vi.exception.MedicoNotFoundException;
import com.dr_commerce.e_commerce.vi.model.Medico;
import com.dr_commerce.e_commerce.vi.repository.MedicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MedicoService {

    private final MedicoRepository medicoRepository;

    public MedicoService(MedicoRepository medicoRepository) {
        this.medicoRepository = medicoRepository;
    }

    public List<MedicoResponseDto> obtenerTodos() {
        return medicoRepository.findAll().stream().map(this::aResponseDto).toList();
    }

    public MedicoResponseDto obtenerPorId(Long id) {
        return medicoRepository.findById(id)
                .map(this::aResponseDto)
                .orElseThrow(() -> new MedicoNotFoundException(id));
    }

    @Transactional
    public MedicoResponseDto crear(MedicoRequestDto request) {
        if (medicoRepository.existsByMatricula(request.getMatricula())) {
            throw new IllegalArgumentException("Ya existe un médico registrado con esa matrícula");
        }
        Medico medico = new Medico();
        completarMedico(medico, request);
        return aResponseDto(medicoRepository.save(medico));
    }

    @Transactional
    public MedicoResponseDto actualizar(Long id, MedicoRequestDto request) {
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new MedicoNotFoundException(id));
        completarMedico(medico, request);
        return aResponseDto(medicoRepository.save(medico));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!medicoRepository.existsById(id)) {
            throw new MedicoNotFoundException(id);
        }
        medicoRepository.deleteById(id);
    }

    private void completarMedico(Medico medico, MedicoRequestDto request) {
        medico.setNombre(request.getNombre());
        medico.setApellido(request.getApellido());
        medico.setMatricula(request.getMatricula());
        medico.setEspecialidad(request.getEspecialidad());
    }

    private MedicoResponseDto aResponseDto(Medico medico) {
        return new MedicoResponseDto(
                medico.getId(),
                medico.getNombre(),
                medico.getApellido(),
                medico.getMatricula(),
                medico.getEspecialidad()
        );
    }
}
