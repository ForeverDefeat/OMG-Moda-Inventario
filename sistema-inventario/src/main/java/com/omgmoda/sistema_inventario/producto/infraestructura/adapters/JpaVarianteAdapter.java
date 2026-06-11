package com.omgmoda.sistema_inventario.producto.infraestructura.adapters;

import com.omgmoda.sistema_inventario.producto.dominio.Producto;
import com.omgmoda.sistema_inventario.producto.dominio.VarianteProducto;
import com.omgmoda.sistema_inventario.producto.dominio.ports.IVarianteRepository;
import com.omgmoda.sistema_inventario.producto.infraestructura.entities.ProductoJpaEntity;
import com.omgmoda.sistema_inventario.producto.infraestructura.entities.VarianteJpaEntity;
import com.omgmoda.sistema_inventario.shared.dominio.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida que implementa IVarianteRepository.
 * Traduce entre entidades de dominio y entidades JPA.
 * Es la única clase de infraestructura que conoce ambos mundos.
 */
public class JpaVarianteAdapter implements IVarianteRepository {

    private final VarianteJpaRepository jpaRepository;

    public JpaVarianteAdapter(VarianteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public VarianteProducto save(VarianteProducto variante) {
        VarianteJpaEntity entity = toEntity(variante);
        VarianteJpaEntity saved = jpaRepository.save(entity);
        variante.setId(saved.getId());
        variante.getProducto().setId(saved.getProducto().getId());
        return variante;
    }

    @Override
    public Optional<VarianteProducto> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<VarianteProducto> findByProductoId(Long idProducto) {
        return jpaRepository.findByProductoId(idProducto)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<VarianteProducto> findByFiltros(String talla, String color, String categoria) {
        return jpaRepository.findByFiltros(talla, color, categoria)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<VarianteProducto> findBajoStock() {
        return jpaRepository.findBajoStock()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        if (!jpaRepository.existsById(id))
            throw new NotFoundException("Variante no encontrada con id: " + id);
        jpaRepository.deleteById(id);
    }

    // ── Mapeos entre dominio ↔ JPA ─────────────────────────────────────────────

    private VarianteJpaEntity toEntity(VarianteProducto v) {
        VarianteJpaEntity entity = new VarianteJpaEntity();
        entity.setId(v.getId());

        ProductoJpaEntity productoEntity = new ProductoJpaEntity(
                v.getProducto().getNombre(),
                v.getProducto().getCategoria(),
                v.getProducto().getMarca(),
                v.getProducto().getImageUrl()
        );
        productoEntity.setId(v.getProducto().getId());

        entity.setProducto(productoEntity);
        entity.setTalla(v.getTalla());
        entity.setColor(v.getColor());
        entity.setMaterial(v.getMaterial());
        entity.setPrecioCosto(v.getPrecioCosto());
        entity.setPrecioVenta(v.getPrecioVenta());
        entity.setStockActual(v.getStockActual());
        entity.setStockMinimo(v.getStockMinimo());
        return entity;
    }

    private VarianteProducto toDomain(VarianteJpaEntity e) {
        Producto producto = new Producto(
                e.getProducto().getId(),
                e.getProducto().getNombre(),
                e.getProducto().getCategoria(),
                e.getProducto().getMarca(),
                e.getProducto().getImageUrl()
        );
        return new VarianteProducto(
                e.getId(),
                producto,
                e.getTalla(),
                e.getColor(),
                e.getMaterial(),
                e.getPrecioCosto(),
                e.getPrecioVenta(),
                e.getStockActual(),
                e.getStockMinimo()
        );
    }
}
