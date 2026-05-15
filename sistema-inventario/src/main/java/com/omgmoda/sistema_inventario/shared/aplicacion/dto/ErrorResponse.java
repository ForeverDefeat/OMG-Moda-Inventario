package com.omgmoda.sistema_inventario.shared.aplicacion.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO estándar para todas las respuestas de error de la API REST.
 * Devuelto por GlobalExceptionHandler en cualquier situación de error.
 *
 * Estructura JSON de ejemplo:
 * {
 *   "timestamp": "2024-11-15T10:30:00",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Stock insuficiente. Disponible: 2, solicitado: 5.",
 *   "path": "/api/v1/ventas",
 *   "detalles": []
 * }
 */
public record ErrorResponse(

        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,

        /**
         * Lista de mensajes detallados usada para errores de validación
         * (@Valid). Vacía para errores de negocio simples.
         */
        List<String> detalles

) {
    /**
     * Constructor de conveniencia para errores simples sin detalles de validación.
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                List.of()
        );
    }

    /**
     * Constructor de conveniencia para errores de validación con lista de campos.
     */
    public static ErrorResponse ofValidation(int status,
                                              String error,
                                              String message,
                                              String path,
                                              List<String> detalles) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                detalles
        );
    }
}
