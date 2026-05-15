package com.omgmoda.sistema_inventario.producto.aplicacion.usecases;

import com.omgmoda.sistema_inventario.producto.aplicacion.dto.VarianteResponseDTO;
import com.omgmoda.sistema_inventario.producto.aplicacion.ports.IBuscarVariantesUseCase;
import com.omgmoda.sistema_inventario.producto.dominio.VarianteProducto;
import com.omgmoda.sistema_inventario.producto.dominio.ports.IVarianteRepository;

import java.util.List;

/**
 * Implementación del caso de uso: Buscar Variantes con filtros (CU-05, RF08).
 * Aplica los filtros técnicos recibidos del controlador y mapea
 * las entidades de dominio a DTOs de respuesta.
 */
public class BuscarVariantesUseCaseImpl implements IBuscarVariantesUseCase {

    private final IVarianteRepository varianteRepository;

    public BuscarVariantesUseCaseImpl(IVarianteRepository varianteRepository) {
        this.varianteRepository = varianteRepository;
    }

    @Override
    public List<VarianteResponseDTO> buscar(String talla, String color, String categoria) {
        return varianteRepository
                .findByFiltros(talla, color, categoria)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public List<VarianteResponseDTO> buscarBajoStock() {
        return varianteRepository
                .findBajoStock()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ── Mapper interno ─────────────────────────────────────────────────────────

    private VarianteResponseDTO toDTO(VarianteProducto v) {
        return new VarianteResponseDTO(
                v.getId(),
                v.getProducto().getId(),
                v.getProducto().getNombre(),
                v.getProducto().getCategoria(),
                v.getProducto().getMarca(),
                v.getTalla(),
                v.getColor(),
                v.getMaterial(),
                v.getPrecioCosto(),
                v.getPrecioVenta(),
                v.getStockActual(),
                v.getStockMinimo(),
                v.getStockStatus()
        );
    }
}
