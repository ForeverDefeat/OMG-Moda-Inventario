package com.omgmoda.sistema_inventario.usuario.aplicacion.dto;

import com.omgmoda.sistema_inventario.usuario.dominio.RolUsuario;

public record UsuarioResponseDTO(
        Long id,
        String nombre,
        String correo,
        RolUsuario rol,
        boolean activo
) {}
