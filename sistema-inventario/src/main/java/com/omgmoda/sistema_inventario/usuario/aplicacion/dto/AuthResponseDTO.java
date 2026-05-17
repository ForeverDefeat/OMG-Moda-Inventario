package com.omgmoda.sistema_inventario.usuario.aplicacion.dto;

import com.omgmoda.sistema_inventario.usuario.dominio.RolUsuario;

import java.time.LocalDateTime;

/**
 * Record de salida devuelto tras un login exitoso.
 * El frontend almacena el token y lo envía en cada
 * petición subsiguiente como Bearer token en el header Authorization.
 */
public record AuthResponseDTO(

        String token,
        String tipo,          // siempre "Bearer"
        RolUsuario rol,
        String nombre,
        String correo,
        LocalDateTime expiracion

) {
    /**
     * Constructor de conveniencia con tipo Bearer por defecto.
     */
    public static AuthResponseDTO of(String token,
                                     RolUsuario rol,
                                     String nombre,
                                     String correo,
                                     LocalDateTime expiracion) {
        return new AuthResponseDTO(token, "Bearer", rol, nombre, correo, expiracion);
    }
}
