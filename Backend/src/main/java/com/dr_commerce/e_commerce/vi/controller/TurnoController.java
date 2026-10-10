package com.dr_commerce.e_commerce.vi.controller;

import com.dr_commerce.e_commerce.vi.dto.TurnoRequestDto;
import com.dr_commerce.e_commerce.vi.dto.TurnoResponseDto;
import com.dr_commerce.e_commerce.vi.model.Especialidad;
import com.dr_commerce.e_commerce.vi.model.EstadoTurno;
import com.dr_commerce.e_commerce.vi.service.TurnoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/turnos")
public class TurnoController {

    private final TurnoService turnoService;

    public TurnoController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }
    // Obtiene los turnos y permite filtrarlos por especialidad, paciente o estado.
    @GetMapping
    public ResponseEntity<List<TurnoResponseDto>> obtenerTodos(
            @RequestParam(required = false) Especialidad especialidad,
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false) EstadoTurno estado) {
        if (especialidad != null) {
            return ResponseEntity.ok(turnoService.buscarPorEspecialidad(especialidad));
        }
        if (pacienteId != null) {
            return ResponseEntity.ok(turnoService.buscarPorPaciente(pacienteId));
        }
        if (estado != null) {
            return ResponseEntity.ok(turnoService.buscarPorEstado(estado));
        }
        return ResponseEntity.ok(turnoService.obtenerTodos());
    }

    // Busca un turno por su ID y lo devuelve.
    @GetMapping("/{id}")
    public ResponseEntity<TurnoResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(turnoService.obtenerPorId(id));
    }

    // Crea un nuevo turno y devuelve su ubicación junto con los datos creados.
    @PostMapping
    public ResponseEntity<TurnoResponseDto> crear(@RequestBody TurnoRequestDto request) {
        TurnoResponseDto turnoCreado = turnoService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(turnoCreado.getId())
                .toUri();
        return ResponseEntity.created(ubicacion).body(turnoCreado);
    }

    // Actualiza un turno existente y devuelve los datos actualizados.
    @PutMapping("/{id}")
    public ResponseEntity<TurnoResponseDto> actualizar(@PathVariable Long id,
                                                       @RequestBody TurnoRequestDto request) {
        return ResponseEntity.ok(turnoService.actualizar(id, request));
    }

    // Elimina un turno por su ID y devuelve una respuesta sin contenido.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        turnoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
