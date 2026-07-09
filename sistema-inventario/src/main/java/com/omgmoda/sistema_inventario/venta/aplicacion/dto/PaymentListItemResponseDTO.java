package com.omgmoda.sistema_inventario.venta.aplicacion.dto;

import com.omgmoda.sistema_inventario.venta.dominio.EstadoPago;
import com.omgmoda.sistema_inventario.venta.dominio.EstadoVenta;
import com.omgmoda.sistema_inventario.venta.dominio.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO que define la estructura de datos intercambiada por la capa de aplicacion/API.
 */
public record PaymentListItemResponseDTO(
        Long idPayment,
        Long idVenta,
        Long idUsuario,
        String vendedorNombre,
        String vendedorCorreo,
        MetodoPago method,
        String providerReference,
        BigDecimal amountDue,
        BigDecimal amountReceived,
        String currency,
        EstadoPago status,
        EstadoVenta estadoVenta,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        LocalDateTime confirmedAt
) {}
