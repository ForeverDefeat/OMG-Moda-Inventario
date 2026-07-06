package com.omgmoda.sistema_inventario.venta.aplicacion.dto;

import java.time.LocalDateTime;

public record PaymentAuditResponseDTO(
        String eventType,
        String previousStatus,
        String newStatus,
        Long userId,
        String userRole,
        String reference,
        String observation,
        LocalDateTime createdAt
) {}
