package com.dr_commerce.e_commerce.vi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "turnos")
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String paciente;

    @Column(nullable = false)
    private String medico;

    private String especialidad;

    @Embedded
    private Fecha fecha;

    @Embedded
    private Direccion direccion;

    private String estado; // ej: "PENDIENTE", "CONFIRMADO", "CANCELADO"
}