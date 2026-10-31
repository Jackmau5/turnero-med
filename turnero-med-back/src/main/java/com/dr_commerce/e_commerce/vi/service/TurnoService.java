package com.dr_commerce.e_commerce.vi.service;

import com.dr_commerce.e_commerce.vi.dto.*;
import com.dr_commerce.e_commerce.vi.exception.*;
import com.dr_commerce.e_commerce.vi.model.*;
import com.dr_commerce.e_commerce.vi.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TurnoService {
    private final TurnoRepository turnos;
    private final MedicoRepository medicos;
    private final PacienteRepository pacientes;
    private final AccesoService acceso;
    public TurnoService(TurnoRepository turnos, MedicoRepository medicos, PacienteRepository pacientes, AccesoService acceso) {
        this.turnos = turnos; this.medicos = medicos; this.pacientes = pacientes; this.acceso = acceso;
    }
    public List<TurnoResponseDto> listar(Especialidad especialidad, Long medicoId, LocalDate dia, EstadoTurno estado, Long pacienteId) {
        if (pacienteId != null) acceso.exigirPacienteOAdmin(pacienteId);
        EstadoTurno filtro = estado == null && pacienteId == null ? EstadoTurno.DISPONIBLE : estado;
        if (filtro != EstadoTurno.DISPONIBLE) acceso.usuarioActual();
        return turnos.filtrar(especialidad, medicoId, dia, filtro).stream()
                .filter(t -> pacienteId == null || (t.getPaciente() != null && pacienteId.equals(t.getPaciente().getId())))
                .filter(t -> filtro != EstadoTurno.DISPONIBLE || esFuturo(t))
                .filter(this::puedeVer)
                .map(DtoMapper::turno).toList();
    }
    public List<TurnoResponseDto> misTurnos() {
        return turnos.findByPacienteIdOrderByFecha_DiaAscFecha_HoraAsc(acceso.pacienteActual())
                .stream().map(DtoMapper::turno).toList();
    }
    public TurnoResponseDto obtenerPorId(Long id) {
        Turno turno = buscar(id);
        if (!puedeVer(turno)) throw new AccessDeniedException("No podés consultar ese turno");
        return DtoMapper.turno(turno);
    }
    private boolean puedeVer(Turno t) {
        if (t.getEstado() == EstadoTurno.DISPONIBLE) return true;
        Usuario u = acceso.usuarioOpcional();
        return u != null && (u.getRol() == Rol.ADMIN ||
                (u.getRol() == Rol.MEDICO && u.getId().equals(t.getMedico().getId())) ||
                (t.getPaciente() != null && u.getId().equals(t.getPaciente().getId())) ||
                (t.getCarrito() != null && u.getId().equals(t.getCarrito().getPaciente().getId())));
    }
    @Transactional
    public TurnoResponseDto crear(TurnoRequestDto request) {
        validar(request);
        acceso.exigirGestionMedico(request.getMedicoId());
        Medico medico = medicos.buscarConBloqueo(request.getMedicoId())
                .orElseThrow(() -> new MedicoNotFoundException(request.getMedicoId()));
        validarHorarioUnico(request, -1L);
        Turno turno = new Turno();
        completar(turno, request, medico);
        turno.setEstado(EstadoTurno.DISPONIBLE);
        return DtoMapper.turno(turnos.save(turno));
    }
    @Transactional
    public TurnoResponseDto actualizar(Long id, TurnoRequestDto request) {
        validar(request);
        Long medicoId = turnos.buscarMedicoId(id).orElseThrow(() -> new TurnoNotFoundException(id));
        acceso.exigirGestionMedico(medicoId);
        if (!medicoId.equals(request.getMedicoId()))
            throw new IllegalArgumentException("No se puede cambiar el médico de un turno; creá otro horario");
        Medico medico = medicos.buscarConBloqueo(request.getMedicoId()).orElseThrow();
        Turno turno = bloquear(id);
        if (turno.getEstado() != EstadoTurno.DISPONIBLE)
            throw new ConflictoException("Solo se pueden modificar horarios disponibles");
        validarHorarioUnico(request, id);
        completar(turno, request, medico);
        return DtoMapper.turno(turno);
    }
    // Baja lógica de un horario ofrecido. No borra reservas del historial.
    @Transactional
    public void eliminar(Long id) {
        Turno turno = bloquear(id);
        acceso.exigirGestionMedico(turno.getMedico().getId());
        if (turno.getEstado() != EstadoTurno.DISPONIBLE)
            throw new ConflictoException("Solo se puede dar de baja un horario disponible");
        turno.setEstado(EstadoTurno.CANCELADO);
    }
    // Cancela la reserva del paciente y vuelve a ofrecer ese horario con otro ID.
    @Transactional
    public TurnoResponseDto cancelarReserva(Long id) {
        Long pacienteId = acceso.pacienteActual();
        pacientes.buscarConBloqueo(pacienteId).orElseThrow(() -> new PacienteNotFoundException(pacienteId));
        Long medicoId = turnos.buscarMedicoId(id).orElseThrow(() -> new TurnoNotFoundException(id));
        medicos.buscarConBloqueo(medicoId).orElseThrow();
        Turno turno = bloquear(id);
        if (turno.getPaciente() == null || !pacienteId.equals(turno.getPaciente().getId()))
            throw new AccessDeniedException("El turno no pertenece a tu cuenta");
        if (turno.getEstado() != EstadoTurno.TOMADO || !esFuturo(turno))
            throw new ConflictoException("Solo podés cancelar turnos confirmados que todavía no comenzaron");
        turno.setEstado(EstadoTurno.CANCELADO);
        Turno nuevo = new Turno();
        nuevo.setMedico(turno.getMedico());
        nuevo.setFecha(new Fecha(turno.getFecha().getDia(), turno.getFecha().getHora()));
        Direccion d = turno.getDireccion();
        nuevo.setDireccion(new Direccion(d.getCalle(), d.getAltura(), d.getCiudad(), d.getCodigoPostal()));
        nuevo.setPrecio(turno.getPrecio());
        nuevo.setEstado(EstadoTurno.DISPONIBLE);
        turnos.save(nuevo);
        return DtoMapper.turno(turno);
    }
    private void completar(Turno t, TurnoRequestDto r, Medico m) {
        t.setMedico(m); t.setFecha(r.getFecha()); t.setDireccion(r.getDireccion()); t.setPrecio(r.getPrecio());
    }
    private void validar(TurnoRequestDto r) {
        if (r.getMedicoId() == null) throw new IllegalArgumentException("medicoId es obligatorio");
        if (r.getPacienteId() != null || (r.getEstado() != null && r.getEstado() != EstadoTurno.DISPONIBLE))
            throw new IllegalArgumentException("Los horarios se crean disponibles. Las reservas se confirman desde el carrito");
        if (r.getFecha() == null || r.getFecha().getDia() == null || r.getFecha().getHora() == null ||
                !LocalDateTime.of(r.getFecha().getDia(), r.getFecha().getHora()).isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("La fecha y la hora son obligatorias y deben ser futuras");
        if (r.getFecha().getHora().getSecond() != 0 || r.getFecha().getHora().getNano() != 0)
            throw new IllegalArgumentException("Indicá la hora con precisión de minutos (HH:mm)");
        if (r.getPrecio() == null || r.getPrecio().signum() < 0 || r.getPrecio().scale() > 2 ||
                r.getPrecio().compareTo(new java.math.BigDecimal("9999999999.99")) > 0)
            throw new IllegalArgumentException("El precio es obligatorio, no puede ser negativo y admite hasta 2 decimales");
        Direccion d = r.getDireccion();
        if (d == null) throw new IllegalArgumentException("La dirección es obligatoria");
        d.setCalle(Validaciones.texto(d.getCalle(), "La calle"));
        d.setCiudad(Validaciones.texto(d.getCiudad(), "La ciudad"));
        if (d.getAltura() == null || d.getAltura() < 0) throw new IllegalArgumentException("La altura no es válida");
    }
    private void validarHorarioUnico(TurnoRequestDto r, Long id) {
        if (turnos.existsByMedicoIdAndFecha_DiaAndFecha_HoraAndEstadoNotAndIdNot(
                r.getMedicoId(), r.getFecha().getDia(), r.getFecha().getHora(), EstadoTurno.CANCELADO, id))
            throw new ConflictoException("El médico ya tiene un turno en ese día y horario");
    }
    private Turno buscar(Long id) { return turnos.findById(id).orElseThrow(() -> new TurnoNotFoundException(id)); }
    private Turno bloquear(Long id) { return turnos.buscarConBloqueo(id).orElseThrow(() -> new TurnoNotFoundException(id)); }
    public static boolean esFuturo(Turno t) {
        return t.getFecha() != null && t.getFecha().getDia() != null && t.getFecha().getHora() != null &&
                LocalDateTime.of(t.getFecha().getDia(), t.getFecha().getHora()).isAfter(LocalDateTime.now());
    }
}
