package com.omgmoda.sistema_inventario.venta.infraestructura.adapters;

import com.omgmoda.sistema_inventario.venta.dominio.EstadoVenta;
import com.omgmoda.sistema_inventario.venta.infraestructura.entities.VentaJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz Spring Data JPA interna del adaptador de venta.
 * Solo visible dentro del paquete de infraestructura.
 */
public interface VentaJpaRepository extends JpaRepository<VentaJpaEntity, Long> {

    @Override
    @EntityGraph(attributePaths = "detalles")
    Optional<VentaJpaEntity> findById(Long id);

    @Override
    @EntityGraph(attributePaths = "detalles")
    List<VentaJpaEntity> findAll();

    @EntityGraph(attributePaths = "detalles")
    List<VentaJpaEntity> findByIdUsuario(Long idUsuario);

    @EntityGraph(attributePaths = "detalles")
    List<VentaJpaEntity> findByEstado(EstadoVenta estado);

    @EntityGraph(attributePaths = "detalles")
    List<VentaJpaEntity> findByIdUsuarioAndEstado(Long idUsuario, EstadoVenta estado);

    @EntityGraph(attributePaths = "detalles")
    List<VentaJpaEntity> findByFechaBetween(LocalDateTime desde, LocalDateTime hasta);

    @EntityGraph(attributePaths = "detalles")
    List<VentaJpaEntity> findByIdUsuarioAndFechaBetween(
            Long idUsuario,
            LocalDateTime desde,
            LocalDateTime hasta
    );
}
