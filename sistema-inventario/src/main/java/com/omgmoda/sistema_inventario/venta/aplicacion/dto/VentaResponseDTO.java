package com.omgmoda.sistema_inventario.venta.aplicacion.dto;

import com.omgmoda.sistema_inventario.venta.dominio.EstadoVenta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Record de salida con los datos completos de una venta.
 * El total se incluye como valor calculado (no persistido) para
 * que el frontend no tenga que recalcularlo.
 */
public record VentaResponseDTO(

        Long idVenta,
        Long idUsuario,
        EstadoVenta estado,
        String metodoPago,
        LocalDateTime fecha,
        List<DetalleVentaResponseDTO> detalles,
        BigDecimal total           // calculado: SUM(cantidad * precioUnitario)

) {
    /**
     * Record anidado para cada línea del detalle en la respuesta.
     */
    public record DetalleVentaResponseDTO(
            Long idDetalle,
            Long idVariante,
            int cantidad,
            BigDecimal precioUnitario,
            BigDecimal subtotal    // calculado: cantidad * precioUnitario
    ) {}
}
