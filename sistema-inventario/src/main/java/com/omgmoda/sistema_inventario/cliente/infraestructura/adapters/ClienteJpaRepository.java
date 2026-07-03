package com.omgmoda.sistema_inventario.cliente.infraestructura.adapters;

import com.omgmoda.sistema_inventario.cliente.infraestructura.entities.ClienteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteJpaRepository extends JpaRepository<ClienteJpaEntity, Long> {

    boolean existsByCorreo(String correo);
}
