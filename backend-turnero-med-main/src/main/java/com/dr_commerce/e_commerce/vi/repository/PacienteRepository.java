package com.dr_commerce.e_commerce.vi.repository;

import com.dr_commerce.e_commerce.vi.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    boolean existsByDni(String dni);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select e from Paciente e where e.id = :id")
    java.util.Optional<Paciente> buscarConBloqueo(@org.springframework.data.repository.query.Param("id") Long id);
}
