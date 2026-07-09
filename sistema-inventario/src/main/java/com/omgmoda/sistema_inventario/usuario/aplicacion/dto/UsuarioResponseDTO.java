package com.omgmoda.sistema_inventario.usuario.aplicacion.dto;

import com.omgmoda.sistema_inventario.usuario.dominio.RolUsuario;

/**
 * DTO que define la estructura de datos intercambiada por la capa de aplicacion/API.
 */
public record UsuarioResponseDTO(
        Long id,
        String nombre,
        String correo,
        RolUsuario rol,
        boolean activo
) {}
