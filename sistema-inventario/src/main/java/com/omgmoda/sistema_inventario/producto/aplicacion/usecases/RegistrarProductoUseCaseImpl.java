package com.omgmoda.sistema_inventario.producto.aplicacion.usecases;

import com.omgmoda.sistema_inventario.producto.aplicacion.dto.CrearProductoDTO;
import com.omgmoda.sistema_inventario.producto.aplicacion.dto.VarianteResponseDTO;
import com.omgmoda.sistema_inventario.producto.aplicacion.ports.IRegistrarProductoUseCase;
import com.omgmoda.sistema_inventario.producto.dominio.Producto;
import com.omgmoda.sistema_inventario.producto.dominio.VarianteProducto;
import com.omgmoda.sistema_inventario.producto.dominio.ports.IVarianteRepository;
import com.omgmoda.sistema_inventario.shared.aplicacion.utils.TextNormalizer;
import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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
        Producto producto = new Producto(
                TextNormalizer.normalizeRequired(dto.nombre(), "El nombre del producto"),
                TextNormalizer.normalizeRequired(dto.categoria(), "La categoria"),
                TextNormalizer.normalizeRequired(dto.marca(), "La marca"),
                TextNormalizer.normalizeOptional(dto.imageUrl())
        );

        // 2. Delegar la creación de cada variante al propio agregado
        Set<String> skusEnSolicitud = new HashSet<>();
        AtomicInteger secuencia = new AtomicInteger(1);
        List<VarianteProducto> variantesDominio = dto.variantes().stream()
                .map(v -> {
                    String talla = TextNormalizer.normalizeRequired(v.talla(), "La talla");
                    String color = TextNormalizer.normalizeRequired(v.color(), "El color");
                    String sku = resolverSku(dto.nombre(), talla, color, v.sku(), secuencia.getAndIncrement());
                    validarSkuDisponible(sku, skusEnSolicitud);
                    return producto.crearVariante(
                        sku,
                        talla,
                        color,
                        TextNormalizer.normalizeOptional(v.material()),
                        v.precioCosto(),
                        v.precioVenta()
                    );
                })
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
                v.getSku(),
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
                v.getStockReservado(),
                v.stockDisponible(),
                v.getStockMinimo(),
                v.getStockStatus()
        );
    }

    private String resolverSku(String nombreProducto, String talla, String color, String skuManual, int secuencia) {
        if (skuManual != null && !skuManual.isBlank()) {
            return normalizarSku(skuManual);
        }
        return "CW-" + limitarParte(slug(nombreProducto), 18)
                + "-" + limitarParte(slug(talla), 6)
                + "-" + limitarParte(slug(color), 8)
                + "-" + String.format("%02d", secuencia);
    }

    private void validarSkuDisponible(String sku, Set<String> skusEnSolicitud) {
        if (!sku.matches("^[A-Z0-9-]{4,40}$")) {
            throw new DomainException("El SKU debe tener entre 4 y 40 caracteres y usar solo letras, numeros y guiones.");
        }
        if (!skusEnSolicitud.add(sku) || varianteRepository.existsBySku(sku)) {
            throw new DomainException("Ya existe una variante con el SKU: " + sku);
        }
    }

    private String normalizarSku(String value) {
        return slug(value);
    }

    private String slug(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return normalized.isBlank() ? "SKU" : normalized;
    }

    private String limitarParte(String value, int maxLength) {
        if (value.length() <= maxLength) return value;
        return value.substring(0, maxLength).replaceAll("-+$", "");
    }
}
