package com.dr_commerce.e_commerce.vi.exception;

public class PacienteNotFoundException extends NotFoundException {

    public PacienteNotFoundException(Long id) {
        super("No se encontró el paciente con id: " + id);
    }
}
