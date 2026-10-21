package com.dr_commerce.e_commerce.vi.service;

import com.dr_commerce.e_commerce.vi.dto.PacienteRequestDto;
import com.dr_commerce.e_commerce.vi.dto.PacienteResponseDto;
import com.dr_commerce.e_commerce.vi.exception.PacienteNotFoundException;
import com.dr_commerce.e_commerce.vi.model.Paciente;
import com.dr_commerce.e_commerce.vi.repository.PacienteRepository;
import com.dr_commerce.e_commerce.vi.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public PacienteService(PacienteRepository pacienteRepository, UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Obtiene todos los pacientes y los convierte a DTO.
    public List<PacienteResponseDto> obtenerTodos() {
        return pacienteRepository.findAll().stream().map(this::aResponseDto).toList();
    }

    // Busca un paciente por su ID o lanza una excepción si no existe.
    public PacienteResponseDto obtenerPorId(Long id) {
        return pacienteRepository.findById(id)
                .map(this::aResponseDto)
                .orElseThrow(() -> new PacienteNotFoundException(id));
    }

    // Crea un paciente, valida sus datos y guarda su contraseña codificada.
    @Transactional
    public PacienteResponseDto crear(PacienteRequestDto request) {
        if (pacienteRepository.existsByDni(request.getDni())) {
            throw new IllegalArgumentException("Ya existe un paciente registrado con ese DNI");
        }
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con ese email");
        }
        Paciente paciente = new Paciente();
        paciente.setPassword(passwordEncoder.encode(request.getPassword()));
        completarPaciente(paciente, request);
        return aResponseDto(pacienteRepository.save(paciente));
    }

    // Busca un paciente, actualiza sus datos y guarda los cambios.
    @Transactional
    public PacienteResponseDto actualizar(Long id, PacienteRequestDto request) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new PacienteNotFoundException(id));
        completarPaciente(paciente, request);
        return aResponseDto(pacienteRepository.save(paciente));
    }

    // Elimina un paciente por su ID.
    @Transactional
    public void eliminar(Long id) {
        if (!pacienteRepository.existsById(id)) {
            throw new PacienteNotFoundException(id);
        }
        pacienteRepository.deleteById(id);
    }

    // Completa los datos del paciente con la información recibida.
    private void completarPaciente(Paciente paciente, PacienteRequestDto request) {
        paciente.setNombre(request.getNombre());
        paciente.setApellido(request.getApellido());
        paciente.setEmail(request.getEmail());
        paciente.setDni(request.getDni());
    }

    // Convierte un paciente a su DTO de respuesta.
    private PacienteResponseDto aResponseDto(Paciente paciente) {
        return new PacienteResponseDto(
                paciente.getId(),
                paciente.getNombre(),
                paciente.getApellido(),
                paciente.getEmail(),
                paciente.getDni());
    }
}
