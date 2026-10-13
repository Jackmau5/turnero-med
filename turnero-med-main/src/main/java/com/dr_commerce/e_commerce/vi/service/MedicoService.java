package com.dr_commerce.e_commerce.vi.service;

import com.dr_commerce.e_commerce.vi.dto.MedicoRequestDto;
import com.dr_commerce.e_commerce.vi.dto.MedicoResponseDto;
import com.dr_commerce.e_commerce.vi.exception.MedicoNotFoundException;
import com.dr_commerce.e_commerce.vi.model.EstadoTurno;
import com.dr_commerce.e_commerce.vi.model.Medico;
import com.dr_commerce.e_commerce.vi.model.Turno;
import com.dr_commerce.e_commerce.vi.repository.MedicoRepository;
import com.dr_commerce.e_commerce.vi.repository.TurnoRepository;
import com.dr_commerce.e_commerce.vi.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.dr_commerce.e_commerce.vi.model.Rol;
import com.dr_commerce.e_commerce.vi.exception.ConflictoException;

@Service
@Transactional(readOnly = true)
public class MedicoService {

    private final MedicoRepository medicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TurnoRepository turnoRepository;
    private final PasswordEncoder passwordEncoder;

    public MedicoService(MedicoRepository medicoRepository, UsuarioRepository usuarioRepository,
            TurnoRepository turnoRepository, PasswordEncoder passwordEncoder) {
        this.medicoRepository = medicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.turnoRepository = turnoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Obtiene los médicos activos y los convierte a DTO.
    public List<MedicoResponseDto> obtenerTodos() {
        return medicoRepository.findByActivoTrue().stream().map(this::aResponseDto).toList();
    }

    // Obtiene todos los médicos, activos e inactivos, para el panel de administración.
    public List<MedicoResponseDto> obtenerTodosConInactivos() {
        return medicoRepository.findAll().stream().map(this::aResponseDto).toList();
    }

    // Busca un médico por su ID o lanza una excepción si no existe.
    public MedicoResponseDto obtenerPorId(Long id) {
        return medicoRepository.findById(id)
                .map(this::aResponseDto)
                .orElseThrow(() -> new MedicoNotFoundException(id));
    }

    // Crea un médico, valida sus datos y guarda su contraseña codificada.
    @Transactional
    public MedicoResponseDto crear(MedicoRequestDto request) {
        request.setEmail(Validaciones.email(request.getEmail()));
        if (medicoRepository.existsByMatricula(request.getMatricula())) {
            throw new ConflictoException("Ya existe un médico registrado con esa matrícula");
        }
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ConflictoException("Ya existe un usuario registrado con ese email");
        }
        Medico medico = new Medico();
        medico.setPassword(passwordEncoder.encode(Validaciones.password(request.getPassword())));
        completarMedico(medico, request);
        return aResponseDto(medicoRepository.save(medico));
    }

    // Busca un médico, actualiza sus datos y guarda los cambios.
    @Transactional
    public MedicoResponseDto actualizar(Long id, MedicoRequestDto request) {
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new MedicoNotFoundException(id));
        if (request.getPassword() != null && !request.getPassword().isBlank())
            medico.setPassword(passwordEncoder.encode(Validaciones.password(request.getPassword())));
        completarMedico(medico, request);
        return aResponseDto(medicoRepository.save(medico));
    }

    // Desactiva un médico sin borrarlo y cancela sus turnos pendientes en la misma transacción.
    @Transactional
    public MedicoResponseDto desactivar(Long id) {
        Medico medico = medicoRepository.buscarConBloqueo(id)
                .orElseThrow(() -> new MedicoNotFoundException(id));
        medico.setActivo(false);
        List<Turno> pendientes = turnoRepository.buscarPorMedicoYEstadosConBloqueo(id,
                List.of(EstadoTurno.DISPONIBLE, EstadoTurno.RESERVADO, EstadoTurno.TOMADO));
        for (Turno turno : pendientes) {
            // Las consultas confirmadas que ya pasaron quedan en el historial sin cambios.
            if (turno.getEstado() == EstadoTurno.TOMADO && !TurnoService.esFuturo(turno)) continue;
            // Un turno reservado está en el carrito de un paciente: se quita para no trabar su confirmación.
            // Uno confirmado conserva paciente y carrito para que siga en su historial como cancelado.
            if (turno.getEstado() == EstadoTurno.RESERVADO) turno.setCarrito(null);
            turno.setEstado(EstadoTurno.CANCELADO);
        }
        return aResponseDto(medico);
    }

    // Reactiva un médico. Los turnos cancelados al desactivarlo no se restauran.
    @Transactional
    public MedicoResponseDto reactivar(Long id) {
        Medico medico = medicoRepository.buscarConBloqueo(id)
                .orElseThrow(() -> new MedicoNotFoundException(id));
        medico.setActivo(true);
        return aResponseDto(medico);
    }

    // Elimina un médico por su ID.
    @Transactional
    public void eliminar(Long id) {
        if (!medicoRepository.existsById(id)) {
            throw new MedicoNotFoundException(id);
        }
        medicoRepository.deleteById(id);
    }

    // Completa los datos del médico con la información recibida.
    private void completarMedico(Medico medico, MedicoRequestDto request) {
        String email = Validaciones.email(request.getEmail());
        usuarioRepository.findByEmail(email).ifPresent(existente -> {
            if (!existente.getId().equals(medico.getId())) throw new ConflictoException("El email ya está en uso");
        });
        medico.setRol(Rol.MEDICO);
        medico.setNombre(Validaciones.texto(request.getNombre(), "El nombre"));
        medico.setApellido(Validaciones.texto(request.getApellido(), "El apellido"));
        medico.setEmail(email);
        String matricula = Validaciones.texto(request.getMatricula(), "La matrícula");
        if (!matricula.equals(medico.getMatricula()) && medicoRepository.existsByMatricula(matricula))
            throw new ConflictoException("La matrícula ya está en uso");
        if (request.getEspecialidad() == null) throw new IllegalArgumentException("La especialidad es obligatoria");
        medico.setMatricula(matricula);
        medico.setEspecialidad(request.getEspecialidad());
    }

    // Convierte un médico a su DTO de respuesta.
    private MedicoResponseDto aResponseDto(Medico medico) {
        return new MedicoResponseDto(
                medico.getId(),
                medico.getNombre(),
                medico.getApellido(),
                medico.getEmail(),
                medico.getMatricula(),
                medico.getEspecialidad(),
                medico.isActivo());
    }
}
