package com.omgmoda.sistema_inventario.shared.dominio.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
