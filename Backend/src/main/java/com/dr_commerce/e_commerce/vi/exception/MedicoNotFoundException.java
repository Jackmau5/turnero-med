package com.dr_commerce.e_commerce.vi.exception;

public class MedicoNotFoundException extends NotFoundException {

    public MedicoNotFoundException(Long id) {
        super("No se encontró el médico con id: " + id);
    }
}
