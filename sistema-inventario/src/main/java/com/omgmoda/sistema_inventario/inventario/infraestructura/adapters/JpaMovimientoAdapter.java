package com.omgmoda.sistema_inventario.inventario.infraestructura.adapters;

import com.omgmoda.sistema_inventario.inventario.dominio.Movimiento;
import com.omgmoda.sistema_inventario.inventario.dominio.TipoMovimiento;
import com.omgmoda.sistema_inventario.inventario.dominio.ports.IMovimientoRepository;
import com.omgmoda.sistema_inventario.inventario.infraestructura.entities.MovimientoJpaEntity;
import com.omgmoda.sistema_inventario.shared.dominio.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida que implementa IMovimientoRepository.
 * Traduce entre entidades de dominio Movimiento y entidades JPA MovimientoJpaEntity.
 * Es la única clase que conoce ambos mundos en este módulo.
 */
public class JpaMovimientoAdapter implements IMovimientoRepository {

    private final MovimientoJpaRepository jpaRepository;

    public JpaMovimientoAdapter(MovimientoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Movimiento save(Movimiento movimiento) {
        MovimientoJpaEntity entity = toEntity(movimiento);
        MovimientoJpaEntity saved = jpaRepository.save(entity);
        movimiento.setId(saved.getId());
        return movimiento;
    }

    @Override
    public Optional<Movimiento> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Movimiento> findByVarianteId(Long idVariante) {
        return jpaRepository.findByIdVariante(idVariante)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Movimiento> findByVarianteIdAndTipo(Long idVariante, TipoMovimiento tipo) {
        return jpaRepository.findByIdVarianteAndTipo(idVariante, tipo)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Movimiento> findByFechaEntre(LocalDateTime desde, LocalDateTime hasta) {
        return jpaRepository.findByFechaBetween(desde, hasta)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Movimiento> findByUsuarioId(Long idUsuario) {
        return jpaRepository.findByIdUsuario(idUsuario)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    // ── Mapeos entre dominio ↔ JPA ─────────────────────────────────────────────

    private MovimientoJpaEntity toEntity(Movimiento m) {
        MovimientoJpaEntity entity = new MovimientoJpaEntity();
        entity.setId(m.getId());
        entity.setIdVariante(m.getIdVariante());
        entity.setIdUsuario(m.getIdUsuario());
        entity.setTipo(m.getTipo());
        entity.setCantidad(m.getCantidad());
        entity.setMotivo(m.getMotivo());
        entity.setFecha(m.getFecha());
        return entity;
    }

    private Movimiento toDomain(MovimientoJpaEntity e) {
        return new Movimiento(
                e.getId(),
                e.getIdVariante(),
                e.getIdUsuario(),
                e.getTipo(),
                e.getCantidad(),
                e.getMotivo(),
                e.getFecha()
        );
    }
}
