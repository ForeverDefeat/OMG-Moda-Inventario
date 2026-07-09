package com.omgmoda.sistema_inventario.usuario.aplicacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO que define la estructura de datos intercambiada por la capa de aplicacion/API.
 */
public record ResetearContraseniaDTO(
        @NotBlank(message = "La nueva contrasenia es obligatoria.")
        @Size(min = 6, max = 72, message = "La contrasenia debe tener entre 6 y 72 caracteres.")
        String nuevaContrasenia
) {}
