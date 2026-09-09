package com.dr_commerce.e_commerce.vi.controller;

import com.dr_commerce.e_commerce.vi.dto.CarritoResponseDto;
import com.dr_commerce.e_commerce.vi.service.CarritoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carritos")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @PostMapping
    public ResponseEntity<CarritoResponseDto> obtenerOCrearActivo(@RequestParam Long pacienteId) {
        return ResponseEntity.ok(carritoService.obtenerOCrearActivo(pacienteId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarritoResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(carritoService.obtenerPorId(id));
    }

    @PostMapping("/{id}/turnos/{turnoId}")
    public ResponseEntity<CarritoResponseDto> agregarTurno(@PathVariable Long id, @PathVariable Long turnoId) {
        return ResponseEntity.ok(carritoService.agregarTurno(id, turnoId));
    }

    @DeleteMapping("/{id}/turnos/{turnoId}")
    public ResponseEntity<CarritoResponseDto> quitarTurno(@PathVariable Long id, @PathVariable Long turnoId) {
        return ResponseEntity.ok(carritoService.quitarTurno(id, turnoId));
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<CarritoResponseDto> confirmar(@PathVariable Long id) {
        return ResponseEntity.ok(carritoService.confirmar(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CarritoResponseDto> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(carritoService.cancelar(id));
    }
}
