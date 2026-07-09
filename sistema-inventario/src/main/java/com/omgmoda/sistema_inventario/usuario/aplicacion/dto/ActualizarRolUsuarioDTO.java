package com.omgmoda.sistema_inventario.usuario.aplicacion.dto;

import com.omgmoda.sistema_inventario.usuario.dominio.RolUsuario;
import jakarta.validation.constraints.NotNull;

/**
 * DTO que define la estructura de datos intercambiada por la capa de aplicacion/API.
 */
public record ActualizarRolUsuarioDTO(
        @NotNull(message = "El rol es obligatorio.")
        RolUsuario rol
) {}
