package com.dr_commerce.e_commerce.vi.model;

import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.time.LocalTime;

@Embeddable
public class Fecha {

    private LocalDate dia;

    private LocalTime hora;

    public Fecha() {}

    public Fecha(LocalDate dia, LocalTime hora) {
        this.dia = dia;
        this.hora = hora;
    }

    public LocalDate getDia() { return dia; }
    public void setDia(LocalDate dia) { this.dia = dia; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
}