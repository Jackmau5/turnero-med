package com.dr_commerce.e_commerce.vi.exception;

public class CarritoNotFoundException extends NotFoundException {

    public CarritoNotFoundException(Long id) {
        super("No se encontró el carrito con id: " + id);
    }
}
