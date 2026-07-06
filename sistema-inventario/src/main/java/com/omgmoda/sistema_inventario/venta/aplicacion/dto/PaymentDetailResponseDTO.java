package com.omgmoda.sistema_inventario.venta.aplicacion.dto;

import com.omgmoda.sistema_inventario.venta.dominio.EstadoReservaStock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PaymentDetailResponseDTO(
        PaymentIntentResponseDTO payment,
        VentaResponseDTO venta,
        String vendedorNombre,
        String vendedorCorreo,
        List<ProductoPagoResponseDTO> productos,
        List<ReservaPagoResponseDTO> reservas
) {
    public record ProductoPagoResponseDTO(
            Long idDetalle,
            Long idVariante,
            String sku,
            String producto,
            String categoria,
            String color,
            String talla,
            int cantidad,
            BigDecimal precioUnitario,
            BigDecimal subtotal
    ) {}

    public record ReservaPagoResponseDTO(
            Long idReserva,
            Long idVariante,
            int cantidad,
            EstadoReservaStock estado,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            LocalDateTime releasedAt
    ) {}
}
