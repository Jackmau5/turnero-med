package com.dr_commerce.e_commerce.vi.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class Direccion {

    private String calle;

    private Integer altura;

    private String ciudad;

    private String codigoPostal;

    public Direccion() {}

    public Direccion(String calle, Integer altura, String ciudad, String codigoPostal) {
        this.calle = calle;
        this.altura = altura;
        this.ciudad = ciudad;
        this.codigoPostal = codigoPostal;
    }

    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }

    public Integer getAltura() { return altura; }
    public void setAltura(Integer altura) { this.altura = altura; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }
}