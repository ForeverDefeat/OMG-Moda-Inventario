package com.omgmoda.sistema_inventario.venta.aplicacion.dto;

import com.omgmoda.sistema_inventario.venta.dominio.EstadoPago;
import com.omgmoda.sistema_inventario.venta.dominio.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentIntentResponseDTO(
        Long idPayment,
        Long idVenta,
        MetodoPago method,
        String provider,
        String providerReference,
        BigDecimal amountDue,
        BigDecimal amountReceived,
        BigDecimal changeAmount,
        String currency,
        EstadoPago status,
        String paymentReference,
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        LocalDateTime confirmedAt
) {}
