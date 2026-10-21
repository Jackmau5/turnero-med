package com.dr_commerce.e_commerce.vi.repository;

import com.dr_commerce.e_commerce.vi.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    boolean existsByMatricula(String matricula);
}
