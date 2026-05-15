package com.omgmoda.sistema_inventario.shared.dominio.exception;

/**
 * Excepción base pura de dominio.
 * Se lanza cuando una regla de negocio es violada dentro de una entidad
 * o aggregate root (ej. stock insuficiente, motivo vacío en ajuste).
 *
 * No depende de Spring ni de ninguna tecnología externa.
 * El GlobalExceptionHandler la captura y la traduce a HTTP 400 Bad Request.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
