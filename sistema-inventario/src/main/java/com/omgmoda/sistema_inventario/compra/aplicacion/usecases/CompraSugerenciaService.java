package com.omgmoda.sistema_inventario.compra.aplicacion.usecases;

import com.omgmoda.sistema_inventario.compra.aplicacion.dto.CompraSugerenciaDTO;
import com.omgmoda.sistema_inventario.producto.aplicacion.dto.VarianteResponseDTO;
import com.omgmoda.sistema_inventario.producto.aplicacion.ports.IBuscarVariantesUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
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
        return buscarVariantesUseCase.buscarBajoStock()
                .stream()
                .map(this::toSuggestion)
                .sorted(Comparator
                        .comparingInt((CompraSugerenciaDTO item) -> prioridadOrden(item.prioridad()))
                        .thenComparing(CompraSugerenciaDTO::producto))
                .toList();
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
}
