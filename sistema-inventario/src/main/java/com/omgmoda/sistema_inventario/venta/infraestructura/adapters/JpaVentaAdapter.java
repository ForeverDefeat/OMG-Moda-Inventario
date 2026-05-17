package com.omgmoda.sistema_inventario.venta.infraestructura.adapters;

import com.omgmoda.sistema_inventario.shared.dominio.exception.NotFoundException;
import com.omgmoda.sistema_inventario.venta.dominio.DetalleVenta;
import com.omgmoda.sistema_inventario.venta.dominio.EstadoVenta;
import com.omgmoda.sistema_inventario.venta.dominio.Venta;
import com.omgmoda.sistema_inventario.venta.dominio.ports.IVentaRepository;
import com.omgmoda.sistema_inventario.venta.infraestructura.entities.DetalleVentaJpaEntity;
import com.omgmoda.sistema_inventario.venta.infraestructura.entities.VentaJpaEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida que implementa IVentaRepository.
 * Traduce entre el Aggregate Root Venta y VentaJpaEntity.
 * El cascade ALL en la entidad JPA garantiza que los detalles
 * se persisten automáticamente al guardar la venta.
 */
public class JpaVentaAdapter implements IVentaRepository {

    private final VentaJpaRepository jpaRepository;

    public JpaVentaAdapter(VentaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Venta save(Venta venta) {
        VentaJpaEntity entity = toEntity(venta);
        VentaJpaEntity saved = jpaRepository.save(entity);
        venta.setId(saved.getId());

        // Propagar los ids generados a los detalles del dominio
        List<DetalleVenta> detallesDominio = venta.getDetalles();
        List<DetalleVentaJpaEntity> detallesJpa = saved.getDetalles();
        for (int i = 0; i < detallesDominio.size(); i++) {
            detallesDominio.get(i).setId(detallesJpa.get(i).getId());
        }
        return venta;
    }

    @Override
    public Optional<Venta> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Venta> findByUsuarioId(Long idUsuario) {
        return jpaRepository.findByIdUsuario(idUsuario)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Venta> findByEstado(EstadoVenta estado) {
        return jpaRepository.findByEstado(estado)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Venta> findByFechaEntre(LocalDateTime desde, LocalDateTime hasta) {
        return jpaRepository.findByFechaBetween(desde, hasta)
                .stream().map(this::toDomain).toList();
    }

    // ── Mapeos entre dominio ↔ JPA ─────────────────────────────────────────────

    private VentaJpaEntity toEntity(Venta venta) {
        VentaJpaEntity entity = new VentaJpaEntity();
        entity.setId(venta.getId());
        entity.setIdUsuario(venta.getIdUsuario());
        entity.setEstado(venta.getEstado());
        entity.setMetodoPago(venta.getMetodoPago());
        entity.setFecha(venta.getFecha());

        List<DetalleVentaJpaEntity> detallesEntity = venta.getDetalles().stream()
                .map(d -> {
                    DetalleVentaJpaEntity de = new DetalleVentaJpaEntity();
                    de.setId(d.getId());
                    de.setVenta(entity);
                    de.setIdVariante(d.getIdVariante());
                    de.setCantidad(d.getCantidad());
                    de.setPrecioUnitario(d.getPrecioUnitario());
                    return de;
                })
                .toList();

        entity.setDetalles(detallesEntity);
        return entity;
    }

    private Venta toDomain(VentaJpaEntity e) {
        List<DetalleVenta> detalles = e.getDetalles().stream()
                .map(d -> new DetalleVenta(
                        d.getId(),
                        d.getIdVariante(),
                        d.getCantidad(),
                        d.getPrecioUnitario()
                ))
                .toList();

        return new Venta(
                e.getId(),
                e.getIdUsuario(),
                e.getEstado(),
                e.getMetodoPago(),
                e.getFecha(),
                detalles
        );
    }
}
