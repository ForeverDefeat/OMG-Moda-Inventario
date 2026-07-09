package com.omgmoda.sistema_inventario.producto.aplicacion.usecases;

import com.omgmoda.sistema_inventario.producto.aplicacion.dto.ActualizarProductoDTO;
import com.omgmoda.sistema_inventario.producto.aplicacion.dto.VarianteResponseDTO;
import com.omgmoda.sistema_inventario.producto.aplicacion.ports.IActualizarProductoUseCase;
import com.omgmoda.sistema_inventario.producto.dominio.VarianteProducto;
import com.omgmoda.sistema_inventario.producto.dominio.ports.IVarianteRepository;
import com.omgmoda.sistema_inventario.shared.aplicacion.utils.TextNormalizer;

import java.util.List;

public class ActualizarProductoUseCaseImpl implements IActualizarProductoUseCase {

    private final IVarianteRepository varianteRepository;

    public ActualizarProductoUseCaseImpl(IVarianteRepository varianteRepository) {
        this.varianteRepository = varianteRepository;
    }

    @Override
    public List<VarianteResponseDTO> actualizar(Long idProducto, ActualizarProductoDTO dto) {
        return varianteRepository.updateProductoVisuals(
                idProducto,
                TextNormalizer.normalizeRequired(dto.nombre(), "El nombre del producto"),
                TextNormalizer.normalizeOptional(dto.imageUrl())
        ).stream()
                .map(this::toDTO)
                .toList();
    }

    private VarianteResponseDTO toDTO(VarianteProducto v) {
        return new VarianteResponseDTO(
                v.getId(),
                v.getProducto().getId(),
                v.getSku(),
                v.getProducto().getNombre(),
                v.getProducto().getCategoria(),
                v.getProducto().getMarca(),
                v.getProducto().getImageUrl(),
                v.getTalla(),
                v.getColor(),
                v.getMaterial(),
                v.getPrecioCosto(),
                v.getPrecioVenta(),
                v.getStockActual(),
                v.getStockReservado(),
                v.stockDisponible(),
                v.getStockMinimo(),
                v.getStockStatus()
        );
    }
}
