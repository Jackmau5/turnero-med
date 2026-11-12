package com.dr_commerce.e_commerce.vi.repository;

import com.dr_commerce.e_commerce.vi.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    boolean existsByMatricula(String matricula);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select e from Medico e where e.id = :id")
    java.util.Optional<Medico> buscarConBloqueo(@org.springframework.data.repository.query.Param("id") Long id);
}
