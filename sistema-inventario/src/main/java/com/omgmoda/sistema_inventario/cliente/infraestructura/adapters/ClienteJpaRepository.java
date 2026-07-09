package com.omgmoda.sistema_inventario.cliente.infraestructura.adapters;

import com.omgmoda.sistema_inventario.cliente.infraestructura.entities.ClienteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA para consultas y persistencia de ClienteJpaRepository.
 */
public interface ClienteJpaRepository extends JpaRepository<ClienteJpaEntity, Long> {

    boolean existsByCorreo(String correo);
}
