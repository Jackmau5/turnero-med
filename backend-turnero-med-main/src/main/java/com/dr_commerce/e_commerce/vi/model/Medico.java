package com.dr_commerce.e_commerce.vi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "medicos")
@Getter
@Setter
@NoArgsConstructor
public class Medico extends Usuario {

    @Column(nullable = false, unique = true)
    private String matricula;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Especialidad especialidad;
}
