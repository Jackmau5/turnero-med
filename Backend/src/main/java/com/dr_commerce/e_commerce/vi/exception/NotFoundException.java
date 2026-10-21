package com.dr_commerce.e_commerce.vi.exception;

// Crea la excepción con el mensaje indicado.
public abstract class NotFoundException extends RuntimeException {

    protected NotFoundException(String message) {
        super(message);
    }
}
