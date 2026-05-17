package com.omgmoda.sistema_inventario.usuario.infraestructura.adapters;

import com.omgmoda.sistema_inventario.usuario.infraestructura.entities.UsuarioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Interfaz Spring Data JPA interna del adaptador de usuario.
 * Solo visible dentro del paquete de infraestructura.
 * Nunca expuesta al dominio ni a la capa de aplicación.
 */
public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, Long> {

    Optional<UsuarioJpaEntity> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}
