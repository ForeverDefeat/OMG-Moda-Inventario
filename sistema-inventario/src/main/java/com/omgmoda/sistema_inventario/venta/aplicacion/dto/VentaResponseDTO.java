package com.omgmoda.sistema_inventario.venta.aplicacion.dto;

import com.omgmoda.sistema_inventario.venta.dominio.EstadoVenta;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Datos completos de una venta.")
public record VentaResponseDTO(

        @Schema(description = "Identificador de la venta.", example = "1")
        Long idVenta,
        @Schema(description = "Identificador del usuario que registro la venta.", example = "2")
        Long idUsuario,
        @Schema(description = "Estado actual de la venta.", example = "COMPLETADA")
        EstadoVenta estado,
        @Schema(description = "Metodo de pago.", example = "EFECTIVO")
        String metodoPago,
        @Schema(description = "Fecha de registro de la venta.")
        LocalDateTime fecha,
        @Schema(description = "Detalles de la venta.")
        List<DetalleVentaResponseDTO> detalles,
        @Schema(description = "Total calculado de la venta.", example = "179.80")
        BigDecimal total

) {
    @Schema(description = "Detalle de una venta.")
    public record DetalleVentaResponseDTO(
            @Schema(description = "Identificador del detalle.", example = "1")
            Long idDetalle,
            @Schema(description = "Identificador de la variante vendida.", example = "1")
            Long idVariante,
            @Schema(description = "Cantidad vendida.", example = "2")
            int cantidad,
            @Schema(description = "Precio unitario aplicado.", example = "89.90")
            BigDecimal precioUnitario,
            @Schema(description = "Subtotal calculado.", example = "179.80")
            BigDecimal subtotal
    ) {}
}
