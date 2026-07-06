package com.omgmoda.sistema_inventario.usuario.dominio.ports;

import com.omgmoda.sistema_inventario.usuario.dominio.Usuario;
import com.omgmoda.sistema_inventario.usuario.dominio.RolUsuario;

import java.util.List;
import java.util.Optional;

/**
 * Output Port — contrato puro para persistencia y consulta de usuarios.
 * El dominio define este contrato; la infraestructura lo implementa.
 * No importa ninguna clase de Spring ni JPA.
 */
public interface IUsuarioRepository {

    /** Persiste un usuario nuevo o actualiza uno existente. */
    Usuario save(Usuario usuario);

    /** Retorna todos los usuarios registrados. */
    List<Usuario> findAll();

    /** Busca un usuario por su identificador. */
    Optional<Usuario> findById(Long id);

    /**
     * Busca un usuario por correo electrónico.
     * Usado en el flujo de autenticación para validar credenciales.
     */
    Optional<Usuario> findByCorreo(String correo);

    /**
     * Verifica si ya existe un usuario registrado con ese correo.
     * Usado para prevenir duplicados en el registro.
     */
    boolean existsByCorreo(String correo);

    /** Cuenta usuarios activos con un rol especifico. */
    long countByRolAndActivoTrue(RolUsuario rol);

    /** Elimina un usuario por su id. Solo accesible para ADMIN. */
    void deleteById(Long id);
}
