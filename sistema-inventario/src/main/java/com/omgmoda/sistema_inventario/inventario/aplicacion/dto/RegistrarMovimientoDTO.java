package com.omgmoda.sistema_inventario.inventario.aplicacion.dto;

import com.omgmoda.sistema_inventario.inventario.dominio.TipoMovimiento;
import jakarta.validation.constraints.*;

/**
 * Record de entrada compartido para registrar entradas y ajustes de stock.
 * Usado en el cuerpo de los endpoints POST /inventario/entrada y /inventario/ajuste.
 */
public record RegistrarMovimientoDTO(

        @NotNull(message = "El id de la variante es obligatorio.")
        Long idVariante,

        @NotNull(message = "El id del usuario es obligatorio.")
        Long idUsuario,

        @NotNull(message = "El tipo de movimiento es obligatorio.")
        TipoMovimiento tipo,

        @NotNull(message = "La cantidad es obligatoria.")
        @Min(value = 0, message = "La cantidad no puede ser negativa.")
        Integer cantidad,

        @Size(max = 150, message = "El motivo no puede superar 150 caracteres.")
        String motivo

) {}
