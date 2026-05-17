package com.omgmoda.sistema_inventario.usuario.aplicacion.usecases;

/**
 * Puerto interno que abstrae el mecanismo de hasheo de contraseñas.
 * Permite que AutenticarUseCaseImpl no dependa directamente de
 * BCryptPasswordEncoder de Spring Security.
 *
 * La implementación concreta se registra en UsuarioModuleConfig.
 */
public interface IPasswordEncoder {

    /**
     * Hashea una contraseña en texto plano.
     * @param raw contraseña sin cifrar.
     * @return hash BCrypt listo para almacenar en BD.
     */
    String encode(String raw);

    /**
     * Verifica si una contraseña en texto plano coincide con un hash almacenado.
     * @param raw    contraseña en texto plano recibida en el login.
     * @param hashed hash almacenado en la base de datos.
     * @return true si coinciden.
     */
    boolean matches(String raw, String hashed);
}
