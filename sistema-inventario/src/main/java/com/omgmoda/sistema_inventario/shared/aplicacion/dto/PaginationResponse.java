package com.omgmoda.sistema_inventario.shared.aplicacion.dto;

import java.util.List;

/**
 * Wrapper genérico para respuestas paginadas de la API REST.
 * Usado en endpoints de listados (variantes, movimientos, ventas, reportes).
 *
 * Estructura JSON de ejemplo:
 * {
 *   "contenido": [...],
 *   "paginaActual": 0,
 *   "totalPaginas": 5,
 *   "totalElementos": 48,
 *   "tamanioPagina": 10,
 *   "esUltima": false
 * }
 *
 * @param <T> tipo del elemento en el listado (ej. VarianteResponseDTO).
 */
public record PaginationResponse<T>(

        List<T> contenido,
        int paginaActual,
        int totalPaginas,
        long totalElementos,
        int tamanioPagina,
        boolean esUltima

) {
    /**
     * Constructor de conveniencia a partir de datos básicos de paginación.
     */
    public static <T> PaginationResponse<T> of(List<T> contenido,
                                                int paginaActual,
                                                int totalPaginas,
                                                long totalElementos,
                                                int tamanioPagina) {
        return new PaginationResponse<>(
                contenido,
                paginaActual,
                totalPaginas,
                totalElementos,
                tamanioPagina,
                paginaActual >= totalPaginas - 1
        );
    }
}
