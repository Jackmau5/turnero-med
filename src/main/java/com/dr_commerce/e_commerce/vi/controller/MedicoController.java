package com.dr_commerce.e_commerce.vi.controller;

import com.dr_commerce.e_commerce.vi.dto.MedicoRequestDto;
import com.dr_commerce.e_commerce.vi.dto.MedicoResponseDto;
import com.dr_commerce.e_commerce.vi.service.MedicoService;
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
@RequestMapping("/api/medicos")
public class MedicoController {

    private final MedicoService medicoService;

    public MedicoController(MedicoService medicoService) {
        this.medicoService = medicoService;
    }

    @GetMapping
    public ResponseEntity<List<MedicoResponseDto>> obtenerTodos() {
        return ResponseEntity.ok(medicoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicoResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(medicoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<MedicoResponseDto> crear(@RequestBody MedicoRequestDto request) {
        MedicoResponseDto medicoCreado = medicoService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(medicoCreado.getId())
                .toUri();
        return ResponseEntity.created(ubicacion).body(medicoCreado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicoResponseDto> actualizar(@PathVariable Long id,
                                                          @RequestBody MedicoRequestDto request) {
        return ResponseEntity.ok(medicoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        medicoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
