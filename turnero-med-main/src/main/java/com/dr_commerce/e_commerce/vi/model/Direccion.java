package com.dr_commerce.e_commerce.vi.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Direccion {

    private String calle;

    private Integer altura;

    private String ciudad;

    private String codigoPostal;
}
