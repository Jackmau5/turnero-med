package com.dr_commerce.e_commerce.vi.service;

import com.dr_commerce.e_commerce.vi.dto.CarritoResponseDto;
import com.dr_commerce.e_commerce.vi.dto.TurnoResponseDto;
import com.dr_commerce.e_commerce.vi.exception.CarritoNotFoundException;
import com.dr_commerce.e_commerce.vi.exception.PacienteNotFoundException;
import com.dr_commerce.e_commerce.vi.exception.TurnoNotFoundException;
import com.dr_commerce.e_commerce.vi.model.Carrito;
import com.dr_commerce.e_commerce.vi.model.EstadoCarrito;
import com.dr_commerce.e_commerce.vi.model.EstadoTurno;
import com.dr_commerce.e_commerce.vi.model.Paciente;
import com.dr_commerce.e_commerce.vi.model.Turno;
import com.dr_commerce.e_commerce.vi.repository.CarritoRepository;
import com.dr_commerce.e_commerce.vi.repository.PacienteRepository;
import com.dr_commerce.e_commerce.vi.repository.TurnoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final TurnoRepository turnoRepository;
    private final PacienteRepository pacienteRepository;

    public CarritoService(CarritoRepository carritoRepository, TurnoRepository turnoRepository,
                           PacienteRepository pacienteRepository) {
        this.carritoRepository = carritoRepository;
        this.turnoRepository = turnoRepository;
        this.pacienteRepository = pacienteRepository;
    }

    public CarritoResponseDto obtenerPorId(Long id) {
        return aResponseDto(buscarCarrito(id));
    }

    @Transactional
    public CarritoResponseDto obtenerOCrearActivo(Long pacienteId) {
        Carrito carrito = carritoRepository.findByPacienteIdAndEstado(pacienteId, EstadoCarrito.ABIERTO)
                .orElseGet(() -> crearCarrito(pacienteId));
        return aResponseDto(carrito);
    }

    @Transactional
    public CarritoResponseDto agregarTurno(Long carritoId, Long turnoId) {
        Carrito carrito = buscarCarritoAbierto(carritoId);
        Turno turno = buscarTurno(turnoId);

        if (turno.getEstado() != EstadoTurno.DISPONIBLE) {
            throw new IllegalArgumentException("El turno no está disponible");
        }
        if (turno.getCarrito() != null) {
            throw new IllegalArgumentException("El turno ya está en otro carrito");
        }

        turno.setCarrito(carrito);
        turnoRepository.save(turno);
        return aResponseDto(carrito);
    }

    @Transactional
    public CarritoResponseDto quitarTurno(Long carritoId, Long turnoId) {
        Carrito carrito = buscarCarritoAbierto(carritoId);
        Turno turno = buscarTurno(turnoId);

        if (turno.getCarrito() == null || !turno.getCarrito().getId().equals(carritoId)) {
            throw new IllegalArgumentException("El turno no pertenece a este carrito");
        }

        turno.setCarrito(null);
        turnoRepository.save(turno);
        return aResponseDto(carrito);
    }

    @Transactional
    public CarritoResponseDto confirmar(Long carritoId) {
        Carrito carrito = buscarCarritoAbierto(carritoId);
        List<Turno> turnos = turnoRepository.findByCarritoId(carritoId);
        if (turnos.isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío");
        }

        List<TurnoResponseDto> turnosConfirmados = turnos.stream().map(turno -> {
            turno.setPaciente(carrito.getPaciente());
            turno.setEstado(EstadoTurno.TOMADO);
            turno.setCarrito(null);
            return aTurnoResponseDto(turno);
        }).toList();
        turnoRepository.saveAll(turnos);

        carrito.setEstado(EstadoCarrito.CONFIRMADO);
        Carrito guardado = carritoRepository.save(carrito);

        return new CarritoResponseDto(guardado.getId(), guardado.getPaciente().getId(), guardado.getEstado(),
                guardado.getFechaCreacion(), turnosConfirmados);
    }

    @Transactional
    public CarritoResponseDto cancelar(Long carritoId) {
        Carrito carrito = buscarCarritoAbierto(carritoId);
        List<Turno> turnos = turnoRepository.findByCarritoId(carritoId);

        List<TurnoResponseDto> turnosLiberados = turnos.stream().map(turno -> {
            turno.setCarrito(null);
            return aTurnoResponseDto(turno);
        }).toList();
        turnoRepository.saveAll(turnos);

        carrito.setEstado(EstadoCarrito.CANCELADO);
        Carrito guardado = carritoRepository.save(carrito);

        return new CarritoResponseDto(guardado.getId(), guardado.getPaciente().getId(), guardado.getEstado(),
                guardado.getFechaCreacion(), turnosLiberados);
    }

    private Carrito crearCarrito(Long pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new PacienteNotFoundException(pacienteId));
        Carrito carrito = new Carrito();
        carrito.setPaciente(paciente);
        carrito.setEstado(EstadoCarrito.ABIERTO);
        carrito.setFechaCreacion(LocalDateTime.now());
        return carritoRepository.save(carrito);
    }

    private Carrito buscarCarrito(Long id) {
        return carritoRepository.findById(id)
                .orElseThrow(() -> new CarritoNotFoundException(id));
    }

    private Carrito buscarCarritoAbierto(Long id) {
        Carrito carrito = buscarCarrito(id);
        if (carrito.getEstado() != EstadoCarrito.ABIERTO) {
            throw new IllegalArgumentException("El carrito ya fue confirmado o cancelado");
        }
        return carrito;
    }

    private Turno buscarTurno(Long id) {
        return turnoRepository.findById(id)
                .orElseThrow(() -> new TurnoNotFoundException(id));
    }

    private CarritoResponseDto aResponseDto(Carrito carrito) {
        List<TurnoResponseDto> turnos = turnoRepository.findByCarritoId(carrito.getId()).stream()
                .map(this::aTurnoResponseDto)
                .toList();
        return new CarritoResponseDto(
                carrito.getId(),
                carrito.getPaciente().getId(),
                carrito.getEstado(),
                carrito.getFechaCreacion(),
                turnos
        );
    }

    private TurnoResponseDto aTurnoResponseDto(Turno turno) {
        return new TurnoResponseDto(
                turno.getId(),
                turno.getPaciente() == null ? null : turno.getPaciente().getId(),
                turno.getMedico() == null ? null : turno.getMedico().getId(),
                turno.getFecha(),
                turno.getDireccion(),
                turno.getEstado()
        );
    }
}
