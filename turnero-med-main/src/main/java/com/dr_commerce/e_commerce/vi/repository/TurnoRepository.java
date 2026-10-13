package com.dr_commerce.e_commerce.vi.repository;

import com.dr_commerce.e_commerce.vi.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TurnoRepository extends JpaRepository<Turno, Long> {
    List<Turno> findByCarritoIdOrderByIdAsc(Long carritoId);
    List<Turno> findByPacienteIdOrderByFecha_DiaAscFecha_HoraAsc(Long pacienteId);
    boolean existsByMedicoId(Long medicoId);
    boolean existsByPacienteId(Long pacienteId);
    @Query("select t.medico.id from Turno t where t.id = :id")
    Optional<Long> buscarMedicoId(@Param("id") Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Turno t where t.id = :id")
    Optional<Turno> buscarConBloqueo(@Param("id") Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Turno t where t.medico.id = :medicoId and t.estado in :estados order by t.id")
    List<Turno> buscarPorMedicoYEstadosConBloqueo(@Param("medicoId") Long medicoId, @Param("estados") List<EstadoTurno> estados);

    @Query("select t from Turno t where (:especialidad is null or t.medico.especialidad = :especialidad) " +
           "and (:medicoId is null or t.medico.id = :medicoId) " +
           "and (:dia is null or t.fecha.dia = :dia) " +
           "and (:estado is null or t.estado = :estado) order by t.fecha.dia, t.fecha.hora, t.id")
    List<Turno> filtrar(@Param("especialidad") Especialidad especialidad, @Param("medicoId") Long medicoId,
                       @Param("dia") LocalDate dia, @Param("estado") EstadoTurno estado);

    boolean existsByMedicoIdAndFecha_DiaAndFecha_HoraAndEstadoNotAndIdNot(
            Long medicoId, LocalDate dia, LocalTime hora, EstadoTurno estado, Long id);

    @Query("select count(t) from Turno t left join t.carrito c where t.fecha.dia = :dia and t.fecha.hora = :hora " +
           "and ((t.estado = com.dr_commerce.e_commerce.vi.model.EstadoTurno.TOMADO and t.paciente.id = :pacienteId) " +
           "or (t.estado = com.dr_commerce.e_commerce.vi.model.EstadoTurno.RESERVADO and c.paciente.id = :pacienteId))")
    long contarCoincidencias(@Param("pacienteId") Long pacienteId, @Param("dia") LocalDate dia, @Param("hora") LocalTime hora);
}
