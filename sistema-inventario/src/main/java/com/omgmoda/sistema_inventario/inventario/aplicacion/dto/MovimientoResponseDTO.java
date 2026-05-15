package com.omgmoda.sistema_inventario.inventario.aplicacion.dto;

import com.omgmoda.sistema_inventario.inventario.dominio.TipoMovimiento;

import java.time.LocalDateTime;

/**
 * Record de salida con los datos de un movimiento registrado.
 * Devuelto tras una entrada o ajuste exitoso, y en consultas de historial.
 */
public record MovimientoResponseDTO(

        Long idMovimiento,
        Long idVariante,
        Long idUsuario,
        TipoMovimiento tipo,
        int cantidad,
        String motivo,
        LocalDateTime fecha,
        int stockResultante

) {}
