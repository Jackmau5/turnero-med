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
import com.dr_commerce.e_commerce.vi.model.Rol;
import com.dr_commerce.e_commerce.vi.exception.ConflictoException;

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
        request.setEmail(Validaciones.email(request.getEmail()));
        if (pacienteRepository.existsByDni(request.getDni())) {
            throw new ConflictoException("Ya existe un paciente registrado con ese DNI");
        }
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ConflictoException("Ya existe un usuario registrado con ese email");
        }
        Paciente paciente = new Paciente();
        paciente.setPassword(passwordEncoder.encode(Validaciones.password(request.getPassword())));
        completarPaciente(paciente, request);
        return aResponseDto(pacienteRepository.save(paciente));
    }

    // Busca un paciente, actualiza sus datos y guarda los cambios.
    @Transactional
    public PacienteResponseDto actualizar(Long id, PacienteRequestDto request) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new PacienteNotFoundException(id));
        if (request.getPassword() != null && !request.getPassword().isBlank())
            paciente.setPassword(passwordEncoder.encode(Validaciones.password(request.getPassword())));
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
        String email = Validaciones.email(request.getEmail());
        usuarioRepository.findByEmail(email).ifPresent(existente -> {
            if (!existente.getId().equals(paciente.getId())) throw new ConflictoException("El email ya está en uso");
        });
        paciente.setRol(Rol.PACIENTE);
        paciente.setNombre(Validaciones.texto(request.getNombre(), "El nombre"));
        paciente.setApellido(Validaciones.texto(request.getApellido(), "El apellido"));
        paciente.setEmail(email);
        String dni = Validaciones.dni(request.getDni());
        if (!dni.equals(paciente.getDni()) && pacienteRepository.existsByDni(dni))
            throw new ConflictoException("El DNI ya está en uso");
        paciente.setDni(dni);
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
