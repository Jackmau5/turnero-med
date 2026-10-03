package com.dr_commerce.e_commerce.vi.repository;

import com.dr_commerce.e_commerce.vi.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    boolean existsByDni(String dni);
}
