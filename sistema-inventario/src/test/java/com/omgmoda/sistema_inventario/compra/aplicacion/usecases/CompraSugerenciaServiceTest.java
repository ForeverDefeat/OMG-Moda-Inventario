package com.omgmoda.sistema_inventario.compra.aplicacion.usecases;

import com.omgmoda.sistema_inventario.producto.aplicacion.dto.VarianteResponseDTO;
import com.omgmoda.sistema_inventario.producto.aplicacion.ports.IBuscarVariantesUseCase;
import com.omgmoda.sistema_inventario.shared.dominio.valueobjects.StockStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CompraSugerenciaServiceTest {

    private final IBuscarVariantesUseCase buscarVariantesUseCase = mock(IBuscarVariantesUseCase.class);
    private final CompraSugerenciaService service = new CompraSugerenciaService(buscarVariantesUseCase, "Proveedor Test");

    @Test
    void generaSugerenciasDesdeVariantesBajoStock() {
        when(buscarVariantesUseCase.buscarBajoStock()).thenReturn(List.of(
                variante(1L, "Vestido", 0, 5, BigDecimal.valueOf(80)),
                variante(2L, "Blazer", 7, 10, BigDecimal.valueOf(120))
        ));

        var result = service.listarSugerencias();

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().prioridad()).isEqualTo("Alta");
        assertThat(result.getFirst().proveedor()).isEqualTo("Proveedor Test");
        assertThat(result.getFirst().costoEstimado()).isPositive();
    }

    private VarianteResponseDTO variante(Long id, String nombre, int stock, int minimo, BigDecimal costo) {
        return new VarianteResponseDTO(
                id,
                id,
                nombre,
                "Vestidos",
                "OMG MODA",
                null,
                "M",
                "Negro",
                "Algodon",
                costo,
                BigDecimal.valueOf(150),
                stock,
                minimo,
                stock <= 0 ? StockStatus.AGOTADO : StockStatus.BAJO_STOCK
        );
    }
}
