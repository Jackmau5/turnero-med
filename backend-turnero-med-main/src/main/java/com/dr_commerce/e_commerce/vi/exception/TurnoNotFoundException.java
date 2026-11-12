package com.dr_commerce.e_commerce.vi.exception;

public class TurnoNotFoundException extends NotFoundException {

    public TurnoNotFoundException(Long id) {
        super("No se encontró el turno con id: " + id);
    }
}
