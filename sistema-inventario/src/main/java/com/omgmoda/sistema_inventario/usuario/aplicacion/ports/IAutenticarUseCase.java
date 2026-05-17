package com.omgmoda.sistema_inventario.usuario.aplicacion.ports;

import com.omgmoda.sistema_inventario.usuario.aplicacion.dto.AuthResponseDTO;
import com.omgmoda.sistema_inventario.usuario.aplicacion.dto.LoginDTO;

/**
 * Input Port — contrato para el flujo completo de autenticación (RNF01).
 * El controlador REST invoca este puerto sin conocer la implementación.
 */
public interface IAutenticarUseCase {

    /**
     * Valida las credenciales del usuario y genera un token JWT.
     *
     * @param dto correo y contraseña en texto plano.
     * @return token JWT, rol, nombre, correo y fecha de expiración.
     * @throws com.omgmoda.shared.dominio.exception.NotFoundException
     *         si no existe un usuario con ese correo.
     * @throws com.omgmoda.shared.dominio.exception.DomainException
     *         si la contraseña no coincide.
     */
    AuthResponseDTO autenticar(LoginDTO dto);
}
