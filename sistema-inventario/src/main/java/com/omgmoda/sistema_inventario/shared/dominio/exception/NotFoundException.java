package com.omgmoda.sistema_inventario.shared.dominio.exception;

/**
 * Excepción de dominio lanzada cuando un agregado o entidad
 * no puede ser localizado por su identificador.
 *
 * Ejemplos de uso:
 *   - VarianteProducto no encontrada por idVariante.
 *   - Venta no encontrada por idVenta.
 *   - Usuario no encontrado por correo en el flujo de autenticación.
 *
 * El GlobalExceptionHandler la captura y la traduce a HTTP 404 Not Found.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
