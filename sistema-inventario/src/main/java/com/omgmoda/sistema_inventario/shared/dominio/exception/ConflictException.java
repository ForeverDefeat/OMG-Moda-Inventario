package com.omgmoda.sistema_inventario.shared.dominio.exception;
/**
 * Excepcion de dominio usada para comunicar un error de negocio controlado.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
