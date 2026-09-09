package com.dr_commerce.e_commerce.vi.repository;

import com.dr_commerce.e_commerce.vi.model.Especialidad;
import com.dr_commerce.e_commerce.vi.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {

    List<Turno> findByMedico_Especialidad(Especialidad especialidad);

    List<Turno> findByCarritoId(Long carritoId);
}
