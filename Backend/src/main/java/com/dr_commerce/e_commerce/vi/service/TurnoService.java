package com.dr_commerce.e_commerce.vi.service;

import com.dr_commerce.e_commerce.vi.dto.TurnoRequestDto;
import com.dr_commerce.e_commerce.vi.dto.TurnoResponseDto;
import com.dr_commerce.e_commerce.vi.exception.MedicoNotFoundException;
import com.dr_commerce.e_commerce.vi.model.Especialidad;
import com.dr_commerce.e_commerce.vi.model.EstadoTurno;
import com.dr_commerce.e_commerce.vi.exception.PacienteNotFoundException;
import com.dr_commerce.e_commerce.vi.exception.TurnoNotFoundException;
import com.dr_commerce.e_commerce.vi.model.Medico;
import com.dr_commerce.e_commerce.vi.model.Paciente;
import com.dr_commerce.e_commerce.vi.model.Turno;
import com.dr_commerce.e_commerce.vi.repository.MedicoRepository;
import com.dr_commerce.e_commerce.vi.repository.PacienteRepository;
import com.dr_commerce.e_commerce.vi.repository.TurnoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;

    public TurnoService(TurnoRepository turnoRepository, PacienteRepository pacienteRepository,
                        MedicoRepository medicoRepository) {
        this.turnoRepository = turnoRepository;
        this.pacienteRepository = pacienteRepository;
        this.medicoRepository = medicoRepository;
    }

    // Obtiene todos los turnos y los convierte a DTO.
    public List<TurnoResponseDto> obtenerTodos() {
        return turnoRepository.findAll().stream().map(this::aResponseDto).toList();
    }

    // Busca un turno por su ID o lanza una excepción si no existe.
    public TurnoResponseDto obtenerPorId(Long id) {
        return turnoRepository.findById(id)
                .map(this::aResponseDto)
                .orElseThrow(() -> new TurnoNotFoundException(id));
    }

    // Busca los turnos según la especialidad del médico.
    public List<TurnoResponseDto> buscarPorEspecialidad(Especialidad especialidad) {
        return turnoRepository.findByMedico_Especialidad(especialidad).stream()
                .map(this::aResponseDto)
                .toList();
    }

    // Busca los turnos asociados a un paciente.
    public List<TurnoResponseDto> buscarPorPaciente(Long pacienteId) {
        return turnoRepository.findByPacienteId(pacienteId).stream()
                .map(this::aResponseDto)
                .toList();
    }

    // Busca los turnos según su estado.
    public List<TurnoResponseDto> buscarPorEstado(EstadoTurno estado) {
        return turnoRepository.findByEstado(estado).stream()
                .map(this::aResponseDto)
                .toList();
    }

    // Crea un turno con los datos recibidos y lo guarda.
    @Transactional
    public TurnoResponseDto crear(TurnoRequestDto request) {
        Turno turno = new Turno();
        completarTurno(turno, request);
        return aResponseDto(turnoRepository.save(turno));
    }

    // Busca un turno, actualiza sus datos y guarda los cambios.
    @Transactional
    public TurnoResponseDto actualizar(Long id, TurnoRequestDto request) {
        Turno turno = turnoRepository.findById(id)
                .orElseThrow(() -> new TurnoNotFoundException(id));
        completarTurno(turno, request);
        return aResponseDto(turnoRepository.save(turno));
    }

    // Elimina un turno por su ID.
    @Transactional
    public void eliminar(Long id) {
        if (!turnoRepository.existsById(id)) {
            throw new TurnoNotFoundException(id);
        }
        turnoRepository.deleteById(id);
    }

    // Completa los datos del turno buscando el paciente y médico correspondientes.
    private void completarTurno(Turno turno, TurnoRequestDto request) {
        turno.setPaciente(buscarPaciente(request.getPacienteId()));
        turno.setMedico(buscarMedico(request.getMedicoId()));
        turno.setFecha(request.getFecha());
        turno.setDireccion(request.getDireccion());
        turno.setEstado(request.getEstado());
    }

    // Convierte un turno a su DTO de respuesta.
    private TurnoResponseDto aResponseDto(Turno turno) {
        return new TurnoResponseDto(
                turno.getId(),
                turno.getPaciente() == null ? null : turno.getPaciente().getId(),
                turno.getMedico() == null ? null : turno.getMedico().getId(),
                turno.getFecha(),
                turno.getDireccion(),
                turno.getEstado()
        );
    }

    // Busca un paciente por su ID o devuelve null si no se especificó.
    private Paciente buscarPaciente(Long id) {
        if (id == null) {
            return null;
        }
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new PacienteNotFoundException(id));
    }

    // Busca un médico por su ID o lanza una excepción si no existe.
    private Medico buscarMedico(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El medicoId es obligatorio");
        }
        return medicoRepository.findById(id)
                .orElseThrow(() -> new MedicoNotFoundException(id));
    }
}
