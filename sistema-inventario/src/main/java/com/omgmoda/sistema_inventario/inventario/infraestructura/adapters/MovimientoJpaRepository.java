package com.omgmoda.sistema_inventario.inventario.infraestructura.adapters;

import com.omgmoda.sistema_inventario.inventario.dominio.TipoMovimiento;
import com.omgmoda.sistema_inventario.inventario.infraestructura.entities.MovimientoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interfaz Spring Data JPA interna del adaptador de inventario.
 * Solo visible dentro del paquete de infraestructura.
 * Nunca expuesta al dominio ni a la capa de aplicación.
 */
public interface MovimientoJpaRepository extends JpaRepository<MovimientoJpaEntity, Long> {

    List<MovimientoJpaEntity> findByIdVariante(Long idVariante);

    List<MovimientoJpaEntity> findByIdVarianteAndTipo(Long idVariante, TipoMovimiento tipo);

    List<MovimientoJpaEntity> findByFechaBetween(LocalDateTime desde, LocalDateTime hasta);

    List<MovimientoJpaEntity> findByIdUsuario(Long idUsuario);
}
