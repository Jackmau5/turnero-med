package com.dr_commerce.e_commerce.vi.controller;

import com.dr_commerce.e_commerce.vi.dto.*;
import com.dr_commerce.e_commerce.vi.model.*;
import com.dr_commerce.e_commerce.vi.service.TurnoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.time.LocalDate;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/turnos")
public class TurnoController {
    private final TurnoService servicio;
    public TurnoController(TurnoService servicio) { this.servicio = servicio; }
    @GetMapping
    public List<TurnoResponseDto> listar(@RequestParam(required = false) Especialidad especialidad,
            @RequestParam(required = false) Long medicoId, @RequestParam(required = false) LocalDate dia,
            @RequestParam(required = false) EstadoTurno estado, @RequestParam(required = false) Long pacienteId) {
        return servicio.listar(especialidad, medicoId, dia, estado, pacienteId);
    }
    @GetMapping("/mis-turnos")
    public List<TurnoResponseDto> misTurnos() { return servicio.misTurnos(); }
    @GetMapping("/{id}")
    public TurnoResponseDto detalle(@PathVariable Long id) { return servicio.obtenerPorId(id); }
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PostMapping
    public ResponseEntity<TurnoResponseDto> crear(@RequestBody TurnoRequestDto request) {
        TurnoResponseDto turno = servicio.crear(request);
        return ResponseEntity.created(URI.create("/api/turnos/" + turno.getId())).body(turno);
    }
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PutMapping("/{id}")
    public TurnoResponseDto actualizar(@PathVariable Long id, @RequestBody TurnoRequestDto request) {
        return servicio.actualizar(id, request);
    }
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id); return ResponseEntity.noContent().build();
    }
    @PreAuthorize("hasRole('PACIENTE')")
    @PostMapping("/{id}/cancelar")
    public TurnoResponseDto cancelarReserva(@PathVariable Long id) { return servicio.cancelarReserva(id); }
}
