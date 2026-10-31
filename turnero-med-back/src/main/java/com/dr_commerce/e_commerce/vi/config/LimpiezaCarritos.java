package com.dr_commerce.e_commerce.vi.config;

import com.dr_commerce.e_commerce.vi.model.EstadoCarrito;
import com.dr_commerce.e_commerce.vi.repository.CarritoRepository;
import com.dr_commerce.e_commerce.vi.service.CarritoService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
@EnableScheduling
@ConditionalOnProperty(name = "app.carrito.limpieza", havingValue = "true", matchIfMissing = true)
public class LimpiezaCarritos {
    private final CarritoRepository repositorio;
    private final CarritoService servicio;
    public LimpiezaCarritos(CarritoRepository repositorio, CarritoService servicio) {
        this.repositorio = repositorio; this.servicio = servicio;
    }
    @Scheduled(fixedDelay = 60000, initialDelay = 60000)
    public void liberarReservasVencidas() {
        for (var carrito : repositorio.findByEstadoAndVenceEnBefore(EstadoCarrito.ABIERTO, LocalDateTime.now()))
            servicio.liberarVencido(carrito.getId());
    }
}
