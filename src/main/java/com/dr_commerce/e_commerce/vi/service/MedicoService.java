package com.dr_commerce.e_commerce.vi.service;

import com.dr_commerce.e_commerce.vi.dto.MedicoRequestDto;
import com.dr_commerce.e_commerce.vi.dto.MedicoResponseDto;
import com.dr_commerce.e_commerce.vi.exception.MedicoNotFoundException;
import com.dr_commerce.e_commerce.vi.model.Medico;
import com.dr_commerce.e_commerce.vi.repository.MedicoRepository;
import com.dr_commerce.e_commerce.vi.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MedicoService {

    private final MedicoRepository medicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public MedicoService(MedicoRepository medicoRepository, UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        this.medicoRepository = medicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
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
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con ese email");
        }
        Medico medico = new Medico();
        medico.setPassword(passwordEncoder.encode(request.getPassword()));
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
        medico.setEmail(request.getEmail());
        medico.setMatricula(request.getMatricula());
        medico.setEspecialidad(request.getEspecialidad());
    }

    private MedicoResponseDto aResponseDto(Medico medico) {
        return new MedicoResponseDto(
                medico.getId(),
                medico.getNombre(),
                medico.getApellido(),
                medico.getEmail(),
                medico.getMatricula(),
                medico.getEspecialidad());
    }
}
