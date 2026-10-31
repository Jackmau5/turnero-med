package com.dr_commerce.e_commerce.vi.repository;

import com.dr_commerce.e_commerce.vi.model.Carrito;
import com.dr_commerce.e_commerce.vi.model.EstadoCarrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    java.util.List<Carrito> findByEstadoAndVenceEnBefore(EstadoCarrito estado, java.time.LocalDateTime ahora);

    Optional<Carrito> findByPacienteIdAndEstado(Long pacienteId, EstadoCarrito estado);
    @org.springframework.data.jpa.repository.Query("select c.paciente.id from Carrito c where c.id = :id")
    java.util.Optional<Long> buscarPacienteId(@org.springframework.data.repository.query.Param("id") Long id);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select e from Carrito e where e.id = :id")
    java.util.Optional<Carrito> buscarConBloqueo(@org.springframework.data.repository.query.Param("id") Long id);
}
