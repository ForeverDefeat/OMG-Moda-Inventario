package com.omgmoda.sistema_inventario.producto.aplicacion.usecases;

import com.omgmoda.sistema_inventario.producto.aplicacion.dto.CrearProductoDTO;
import com.omgmoda.sistema_inventario.producto.aplicacion.dto.VarianteResponseDTO;
import com.omgmoda.sistema_inventario.producto.aplicacion.ports.IRegistrarProductoUseCase;
import com.omgmoda.sistema_inventario.producto.dominio.Producto;
import com.omgmoda.sistema_inventario.producto.dominio.VarianteProducto;
import com.omgmoda.sistema_inventario.producto.dominio.ports.IVarianteRepository;

import java.util.List;

/**
 * Implementación del caso de uso: Registrar Producto con Variantes.
 * Orquesta la creación del Aggregate Root Producto, delega la creación
 * de variantes al propio agregado y persiste mediante el Output Port.
 * No importa ninguna clase de Spring (sin @Service aquí).
 */
public class RegistrarProductoUseCaseImpl implements IRegistrarProductoUseCase {

    private final IVarianteRepository varianteRepository;

    public RegistrarProductoUseCaseImpl(IVarianteRepository varianteRepository) {
        this.varianteRepository = varianteRepository;
    }

    @Override
    public List<VarianteResponseDTO> registrar(CrearProductoDTO dto) {

        // 1. Crear el Aggregate Root en el dominio
        Producto producto = new Producto(dto.nombre(), dto.categoria(), dto.marca(), dto.imageUrl());

        // 2. Delegar la creación de cada variante al propio agregado
        List<VarianteProducto> variantesDominio = dto.variantes().stream()
                .map(v -> producto.crearVariante(
                        v.talla(),
                        v.color(),
                        v.material(),
                        v.precioCosto(),
                        v.precioVenta()
                ))
                .toList();

        // 3. Persistir cada variante mediante el Output Port
        List<VarianteProducto> persistidas = variantesDominio.stream()
                .map(varianteRepository::save)
                .toList();

        // 4. Mapear a DTOs de respuesta
        return persistidas.stream()
                .map(v -> toDTO(v, producto))
                .toList();
    }

    // ── Mapper interno ─────────────────────────────────────────────────────────

    private VarianteResponseDTO toDTO(VarianteProducto v, Producto p) {
        return new VarianteResponseDTO(
                v.getId(),
                p.getId(),
                p.getNombre(),
                p.getCategoria(),
                p.getMarca(),
                p.getImageUrl(),
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
