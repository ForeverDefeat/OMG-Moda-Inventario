package com.omgmoda.sistema_inventario.compra.aplicacion.usecases;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.omgmoda.sistema_inventario.compra.aplicacion.dto.CompraSugerenciaDTO;
import com.omgmoda.sistema_inventario.producto.aplicacion.dto.VarianteResponseDTO;
import com.omgmoda.sistema_inventario.producto.aplicacion.ports.IBuscarVariantesUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CompraSugerenciaService {

    private final IBuscarVariantesUseCase buscarVariantesUseCase;
    private final String proveedorDefault;

    public CompraSugerenciaService(IBuscarVariantesUseCase buscarVariantesUseCase,
                                   @Value("${clothwise.compras.proveedor-default:Proveedor pendiente}") String proveedorDefault) {
        this.buscarVariantesUseCase = buscarVariantesUseCase;
        this.proveedorDefault = proveedorDefault;
    }

    public List<CompraSugerenciaDTO> listarSugerencias() {
        List<CompraSugerenciaDTO> sugerencias = buscarVariantesUseCase.buscarBajoStock()
                .stream()
                .map(this::toSuggestion)
                .sorted(this::compararSugerencias)
                .toList();
        return ImmutableList.copyOf(sugerencias);
    }

    private CompraSugerenciaDTO toSuggestion(VarianteResponseDTO variante) {
        int objetivoStock = Math.max(variante.stockMinimo() * 3, variante.stockMinimo() + 10);
        int cantidad = Math.max(objetivoStock - variante.stockActual(), variante.stockMinimo());
        String prioridad = prioridad(variante);
        String motivo = variante.stockActual() <= 0
                ? "Sin stock disponible"
                : "Stock bajo: " + variante.stockActual() + " de minimo " + variante.stockMinimo();

        return new CompraSugerenciaDTO(
                variante.idVariante(),
                variante.nombreProducto() + " " + variante.color() + " " + variante.talla(),
                proveedorDefault,
                cantidad,
                variante.precioCosto().multiply(BigDecimal.valueOf(cantidad)),
                prioridad,
                motivo
        );
    }

    private String prioridad(VarianteResponseDTO variante) {
        if (variante.stockActual() <= 0) return "Alta";
        if (variante.stockActual() <= Math.max(1, variante.stockMinimo() / 2)) return "Alta";
        if (variante.stockActual() <= variante.stockMinimo()) return "Media";
        return "Baja";
    }

    private int prioridadOrden(String prioridad) {
        return switch (prioridad) {
            case "Alta" -> 0;
            case "Media" -> 1;
            default -> 2;
        };
    }

    private int compararSugerencias(CompraSugerenciaDTO left, CompraSugerenciaDTO right) {
        return ComparisonChain.start()
                .compare(prioridadOrden(left.prioridad()), prioridadOrden(right.prioridad()))
                .compare(left.producto(), right.producto())
                .result();
    }
}
