package com.dr_commerce.e_commerce.vi.service;

import com.dr_commerce.e_commerce.vi.dto.*;
import com.dr_commerce.e_commerce.vi.exception.*;
import com.dr_commerce.e_commerce.vi.model.*;
import com.dr_commerce.e_commerce.vi.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CarritoService {
    private final CarritoRepository carritos;
    private final TurnoRepository turnos;
    private final PacienteRepository pacientes;
    private final AccesoService acceso;
    private final int minutosReserva;
    public CarritoService(CarritoRepository carritos, TurnoRepository turnos, PacienteRepository pacientes,
                          AccesoService acceso, @Value("${app.carrito.minutos-reserva:15}") int minutosReserva) {
        this.carritos = carritos; this.turnos = turnos; this.pacientes = pacientes; this.acceso = acceso;
        this.minutosReserva = minutosReserva;
    }
    public CarritoResponseDto obtenerPorId(Long id) {
        Carrito carrito = buscar(id);
        acceso.exigirPaciente(carrito.getPaciente().getId());
        return respuesta(carrito);
    }
    @Transactional
    public CarritoResponseDto obtenerOCrearActivo(Long pacienteId) {
        if (pacienteId == null) pacienteId = acceso.pacienteActual();
        acceso.exigirPaciente(pacienteId);
        Paciente paciente = pacientes.buscarConBloqueo(pacienteId)
                .orElseThrow(() -> new PacienteNotFoundException(acceso.pacienteActual()));
        Carrito carrito = carritos.findByPacienteIdAndEstado(pacienteId, EstadoCarrito.ABIERTO).orElse(null);
        if (carrito != null && vencido(carrito)) {
            liberar(carrito);
            carrito.setEstado(EstadoCarrito.CANCELADO);
            carrito = null;
        }
        if (carrito == null) {
            carrito = new Carrito();
            carrito.setPaciente(paciente);
            carrito.setEstado(EstadoCarrito.ABIERTO);
            carrito.setFechaCreacion(LocalDateTime.now());
            carrito.setVenceEn(LocalDateTime.now().plusMinutes(minutosReserva));
            carritos.save(carrito);
        }
        return respuesta(carrito);
    }
    @Transactional
    public CarritoResponseDto agregarTurno(Long id, Long turnoId) {
        Carrito carrito = abiertoPropio(id);
        Turno turno = bloquearTurno(turnoId);
        if (turno.getCarrito() != null && carrito.getId().equals(turno.getCarrito().getId()))
            throw new ConflictoException("Ese turno ya está en tu carrito");
        if (turno.getEstado() != EstadoTurno.DISPONIBLE || !TurnoService.esFuturo(turno))
            throw new ConflictoException("Turno no disponible");
        if (turnos.contarCoincidencias(carrito.getPaciente().getId(), turno.getFecha().getDia(), turno.getFecha().getHora()) > 0)
            throw new ConflictoException("Ya tenés un turno reservado en ese mismo día y horario");
        turno.setEstado(EstadoTurno.RESERVADO);
        turno.setCarrito(carrito);
        return respuesta(carrito);
    }
    @Transactional
    public CarritoResponseDto quitarTurno(Long id, Long turnoId) {
        Carrito carrito = abiertoPropio(id);
        Turno turno = bloquearTurno(turnoId);
        if (turno.getCarrito() == null || !id.equals(turno.getCarrito().getId()) || turno.getEstado() != EstadoTurno.RESERVADO)
            throw new ConflictoException("El turno no pertenece a este carrito abierto");
        liberarTurno(turno);
        return respuesta(carrito);
    }
    @Transactional
    public CarritoResponseDto vaciar(Long id) {
        Carrito carrito = abiertoPropio(id);
        liberar(carrito);
        return respuesta(carrito);
    }
    @Transactional
    public CarritoResponseDto confirmar(Long id) {
        Carrito carrito = abiertoPropio(id);
        List<Turno> lista = turnos.findByCarritoIdOrderByIdAsc(id);
        if (lista.isEmpty()) throw new ConflictoException("El carrito está vacío");
        for (Turno item : lista) {
            Turno turno = bloquearTurno(item.getId());
            if (turno.getEstado() != EstadoTurno.RESERVADO || !TurnoService.esFuturo(turno))
                throw new ConflictoException("Algún turno dejó de estar disponible o su fecha ya pasó");
            turno.setPaciente(carrito.getPaciente());
            turno.setEstado(EstadoTurno.TOMADO);
            // Se conserva carrito_id para poder consultar la confirmación posteriormente.
        }
        carrito.setEstado(EstadoCarrito.CONFIRMADO);
        return respuesta(carrito);
    }
    @Transactional
    public CarritoResponseDto cancelar(Long id) {
        Carrito carrito = bloquearPropio(id);
        if (carrito.getEstado() != EstadoCarrito.ABIERTO) throw new ConflictoException("El carrito ya está cerrado");
        liberar(carrito);
        carrito.setEstado(EstadoCarrito.CANCELADO);
        return respuesta(carrito);
    }
    // Lo llama el proceso de limpieza, sin depender de una sesión de usuario.
    @Transactional
    public void liberarVencido(Long id) {
        Long pacienteId = carritos.buscarPacienteId(id).orElseThrow(() -> new CarritoNotFoundException(id));
        pacientes.buscarConBloqueo(pacienteId).orElseThrow();
        Carrito carrito = carritos.buscarConBloqueo(id).orElseThrow();
        if (carrito.getEstado() == EstadoCarrito.ABIERTO && vencido(carrito)) {
            liberar(carrito);
            carrito.setEstado(EstadoCarrito.CANCELADO);
        }
    }
    private Carrito abiertoPropio(Long id) {
        Carrito carrito = bloquearPropio(id);
        if (carrito.getEstado() != EstadoCarrito.ABIERTO) throw new ConflictoException("El carrito ya fue confirmado o cancelado");
        if (vencido(carrito)) throw new ConflictoException("El carrito venció. Solicitá un carrito activo nuevamente");
        return carrito;
    }
    private Carrito bloquearPropio(Long id) {
        Long pacienteId = acceso.pacienteActual();
        pacientes.buscarConBloqueo(pacienteId).orElseThrow(() -> new PacienteNotFoundException(pacienteId));
        Carrito carrito = carritos.buscarConBloqueo(id).orElseThrow(() -> new CarritoNotFoundException(id));
        acceso.exigirPaciente(carrito.getPaciente().getId());
        return carrito;
    }
    private boolean vencido(Carrito c) {
        LocalDateTime limite = c.getVenceEn() != null ? c.getVenceEn() : c.getFechaCreacion().plusMinutes(minutosReserva);
        return !limite.isAfter(LocalDateTime.now());
    }
    private void liberar(Carrito c) {
        for (Turno t : turnos.findByCarritoIdOrderByIdAsc(c.getId())) liberarTurno(bloquearTurno(t.getId()));
    }
    private void liberarTurno(Turno t) {
        t.setCarrito(null); t.setPaciente(null); t.setEstado(EstadoTurno.DISPONIBLE);
    }
    private Turno bloquearTurno(Long id) { return turnos.buscarConBloqueo(id).orElseThrow(() -> new TurnoNotFoundException(id)); }
    private Carrito buscar(Long id) { return carritos.findById(id).orElseThrow(() -> new CarritoNotFoundException(id)); }
    private CarritoResponseDto respuesta(Carrito c) {
        List<TurnoResponseDto> lista = turnos.findByCarritoIdOrderByIdAsc(c.getId()).stream().map(DtoMapper::turno).toList();
        BigDecimal total = lista.stream().filter(t -> t.getEstado() != EstadoTurno.CANCELADO)
                .map(TurnoResponseDto::getPrecio).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CarritoResponseDto(c.getId(), c.getPaciente().getId(), c.getEstado(), c.getFechaCreacion(), lista, total, c.getVenceEn());
    }
}
