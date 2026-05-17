package com.omgmoda.sistema_inventario.venta.infraestructura.adapters;

import com.omgmoda.sistema_inventario.venta.dominio.EstadoVenta;
import com.omgmoda.sistema_inventario.venta.infraestructura.entities.VentaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interfaz Spring Data JPA interna del adaptador de venta.
 * Solo visible dentro del paquete de infraestructura.
 */
public interface VentaJpaRepository extends JpaRepository<VentaJpaEntity, Long> {

    List<VentaJpaEntity> findByIdUsuario(Long idUsuario);

    List<VentaJpaEntity> findByEstado(EstadoVenta estado);

    List<VentaJpaEntity> findByFechaBetween(LocalDateTime desde, LocalDateTime hasta);
}
