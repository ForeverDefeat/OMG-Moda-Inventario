package com.omgmoda.sistema_inventario.usuario.aplicacion.dto;

import com.omgmoda.sistema_inventario.usuario.dominio.RolUsuario;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO que define la estructura de datos intercambiada por la capa de aplicacion/API.
 */
@Schema(description = "Respuesta de autenticacion con token JWT.")
public record AuthResponseDTO(

        @Schema(description = "Token JWT usado en Authorization: Bearer <token>.")
        String token,

        @Schema(description = "Tipo de token.", example = "Bearer")
        String tipo,

        @Schema(description = "Rol del usuario autenticado.", example = "ADMIN")
        RolUsuario rol,

        @Schema(description = "Nombre del usuario autenticado.", example = "Administrador")
        String nombre,

        @Schema(description = "Correo del usuario autenticado.", example = "admin@omgmoda.com")
        String correo,

        @Schema(description = "Fecha y hora de expiracion del token.")
        LocalDateTime expiracion

) {
    public static AuthResponseDTO of(String token,
                                     RolUsuario rol,
                                     String nombre,
                                     String correo,
                                     LocalDateTime expiracion) {
        return new AuthResponseDTO(token, "Bearer", rol, nombre, correo, expiracion);
    }
}
