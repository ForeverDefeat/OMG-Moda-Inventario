package com.omgmoda.sistema_inventario.usuario.aplicacion.usecases;

import java.time.LocalDateTime;

/**
 * Puerto interno que abstrae la generación y validación de tokens JWT.
 * Permite que AutenticarUseCaseImpl no dependa directamente de
 * ninguna librería JWT de infraestructura.
 *
 * La implementación concreta (JwtTokenProvider) se registra en UsuarioModuleConfig.
 */
public interface IJwtTokenProvider {

    /**
     * Genera un token JWT firmado con las credenciales del usuario.
     * @param correo correo del usuario (subject del token).
     * @param rol    nombre del rol (claim adicional).
     * @return token JWT como String.
     */
    String generarToken(String correo, String rol);

    /**
     * Extrae el correo (subject) del token.
     * @param token JWT firmado.
     * @return correo del usuario.
     */
    String extraerCorreo(String token);

    /**
     * Retorna la fecha y hora de expiración del token.
     * @param token JWT firmado.
     * @return LocalDateTime de expiración.
     */
    LocalDateTime obtenerExpiracion(String token);

    /**
     * Extrae el rol almacenado en el token JWT.
     * @param token JWT firmado.
     * @return nombre del rol.
     */
    String extraerRol(String token);

    /**
     * Verifica que el token sea válido: firma correcta y no expirado.
     * @param token JWT a validar.
     * @return true si el token es válido.
     */
    boolean esValido(String token);
}
