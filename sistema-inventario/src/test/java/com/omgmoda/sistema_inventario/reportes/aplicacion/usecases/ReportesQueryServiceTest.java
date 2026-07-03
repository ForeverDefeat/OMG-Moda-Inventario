package com.omgmoda.sistema_inventario.reportes.aplicacion.usecases;

import com.omgmoda.sistema_inventario.producto.aplicacion.dto.VarianteResponseDTO;
import com.omgmoda.sistema_inventario.producto.aplicacion.ports.IBuscarVariantesUseCase;
import com.omgmoda.sistema_inventario.shared.dominio.valueobjects.StockStatus;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.VentaResponseDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.ports.IConsultarVentaUseCase;
import com.omgmoda.sistema_inventario.venta.dominio.EstadoVenta;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportesQueryServiceTest {

    private final IConsultarVentaUseCase consultarVentaUseCase = mock(IConsultarVentaUseCase.class);
    private final IBuscarVariantesUseCase buscarVariantesUseCase = mock(IBuscarVariantesUseCase.class);
    private final ReportesQueryService service = new ReportesQueryService(consultarVentaUseCase, buscarVariantesUseCase);

    @Test
    void calculaVentasPorCategoriaDesdeDetallesYVariantes() {
        when(consultarVentaUseCase.buscarPorFechas(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(venta()));
        when(buscarVariantesUseCase.buscar(null, null, null)).thenReturn(List.of(variante(1L, "Camisas")));

        var result = service.ventasCategoria(null, null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Camisas");
        assertThat(result.getFirst().value()).isEqualByComparingTo("179.80");
    }

    @Test
    void stockAlertasUsaCasoDeUsoDeVariantes() {
        when(buscarVariantesUseCase.buscarBajoStock()).thenReturn(List.of(variante(1L, "Camisas")));

        var result = service.stockAlertas();

        assertThat(result).hasSize(1);
    }

    private VentaResponseDTO venta() {
        return new VentaResponseDTO(
                1L,
                2L,
                EstadoVenta.COMPLETADA,
                "EFECTIVO",
                LocalDate.now().atTime(10, 30),
                List.of(new VentaResponseDTO.DetalleVentaResponseDTO(
                        1L,
                        1L,
                        2,
                        BigDecimal.valueOf(89.90),
                        BigDecimal.valueOf(179.80)
                )),
                BigDecimal.valueOf(179.80)
        );
    }

    private VarianteResponseDTO variante(Long id, String categoria) {
        return new VarianteResponseDTO(
                id,
                id,
                "Camisa",
                categoria,
                "OMG MODA",
                null,
                "M",
                "Azul",
                "Algodon",
                BigDecimal.valueOf(45),
                BigDecimal.valueOf(89.90),
                4,
                5,
                StockStatus.BAJO_STOCK
        );
    }
}
