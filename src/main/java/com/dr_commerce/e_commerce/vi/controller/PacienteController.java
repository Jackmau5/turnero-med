package com.dr_commerce.e_commerce.vi.controller;

import com.dr_commerce.e_commerce.vi.dto.PacienteRequestDto;
import com.dr_commerce.e_commerce.vi.dto.PacienteResponseDto;
import com.dr_commerce.e_commerce.vi.service.PacienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping
    public ResponseEntity<List<PacienteResponseDto>> obtenerTodos() {
        return ResponseEntity.ok(pacienteService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<PacienteResponseDto> crear(@RequestBody PacienteRequestDto request) {
        PacienteResponseDto pacienteCreado = pacienteService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(pacienteCreado.getId())
                .toUri();
        return ResponseEntity.created(ubicacion).body(pacienteCreado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PacienteResponseDto> actualizar(@PathVariable Long id,
                                                            @RequestBody PacienteRequestDto request) {
        return ResponseEntity.ok(pacienteService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pacienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
