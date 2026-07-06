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
    void calculaVentasPorCategoriaPorPeriodo() {
        when(consultarVentaUseCase.buscarPorFechas(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(venta()));
        when(buscarVariantesUseCase.buscar(null, null, null)).thenReturn(List.of(variante(1L, "Camisas")));

        var result = service.ventasCategoria("7d");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Camisas");
    }

    @Test
    void rotacionPorPeriodoAgrupaUnidadesVendidas() {
        when(consultarVentaUseCase.buscarPorFechas(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(venta()));
        when(buscarVariantesUseCase.buscar(null, null, null)).thenReturn(List.of(variante(1L, "Camisas")));

        var result = service.rotacionPorPeriodo("Camisas", "today");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().value()).isEqualByComparingTo("2");
    }

    @Test
    void resumenCalculaMetricasConcisasDelNegocio() {
        when(consultarVentaUseCase.buscarPorFechas(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(venta()));
        when(buscarVariantesUseCase.buscar(null, null, null)).thenReturn(List.of(variante(1L, "Camisas")));
        when(buscarVariantesUseCase.buscarBajoStock()).thenReturn(List.of(variante(1L, "Camisas")));

        var result = service.obtenerResumen(
                LocalDateTime.of(2026, 7, 1, 0, 0),
                LocalDateTime.of(2026, 7, 31, 23, 59)
        );

        assertThat(result.ventasMes()).isEqualByComparingTo("179.80");
        assertThat(result.unidadesVendidas()).isEqualTo(2);
        assertThat(result.ticketPromedio()).isEqualByComparingTo("179.80");
        assertThat(result.categoriaPrincipal()).isEqualTo("Camisas");
        assertThat(result.productoMasVendido()).isEqualTo("Camisa");
        assertThat(result.skusConAlerta()).isEqualTo(1);
        assertThat(result.reportesActivos()).isEqualTo(2);
    }

    @Test
    void tendenciaPorRangoAgrupaPorMesCuandoElRangoEsAmplio() {
        when(consultarVentaUseCase.buscarPorFechas(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(venta()));

        var result = service.ventasTendencia(
                LocalDateTime.now().minusDays(90),
                LocalDateTime.now()
        );

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().value()).isEqualByComparingTo("179.80");
    }

    @Test
    void rotacionPorRangoAgrupaUnidadesVendidas() {
        when(consultarVentaUseCase.buscarPorFechas(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(venta()));
        when(buscarVariantesUseCase.buscar(null, null, null)).thenReturn(List.of(variante(1L, "Camisas")));

        var result = service.rotacion(
                LocalDateTime.of(2026, 7, 1, 0, 0),
                LocalDateTime.of(2026, 7, 31, 23, 59),
                "Camisas"
        );

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().value()).isEqualByComparingTo("2");
    }

    @Test
    void ventasCategoriaDetalleIncluyeUnidadesYParticipacion() {
        when(consultarVentaUseCase.buscarPorFechas(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(venta()));
        when(buscarVariantesUseCase.buscar(null, null, null)).thenReturn(List.of(variante(1L, "Camisas")));

        var result = service.ventasCategoriaDetalle(null, null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().categoria()).isEqualTo("Camisas");
        assertThat(result.getFirst().ventas()).isEqualByComparingTo("179.80");
        assertThat(result.getFirst().unidadesVendidas()).isEqualTo(2);
        assertThat(result.getFirst().participacionPorcentaje()).isEqualByComparingTo("100.00");
    }

    @Test
    void productosMasVendidosAgrupaPorProducto() {
        when(consultarVentaUseCase.buscarPorFechas(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(venta()));
        when(buscarVariantesUseCase.buscar(null, null, null)).thenReturn(List.of(variante(1L, "Camisas")));

        var result = service.productosMasVendidos(null, null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().producto()).isEqualTo("Camisa");
        assertThat(result.getFirst().categoria()).isEqualTo("Camisas");
        assertThat(result.getFirst().unidadesVendidas()).isEqualTo(2);
        assertThat(result.getFirst().ingresos()).isEqualByComparingTo("179.80");
    }

    @Test
    void productosBajaRotacionDetectaStockAltoConPocaSalida() {
        when(consultarVentaUseCase.buscarPorFechas(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(buscarVariantesUseCase.buscar(null, null, null))
                .thenReturn(List.of(varianteConStock(1L, "Camisas", 12, 5, StockStatus.NORMAL)));

        var result = service.productosBajaRotacion(null, null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().producto()).isEqualTo("Camisa");
        assertThat(result.getFirst().unidadesVendidas()).isZero();
        assertThat(result.getFirst().stockActual()).isEqualTo(12);
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
        return varianteConStock(id, categoria, 4, 5, StockStatus.BAJO_STOCK);
    }

    private VarianteResponseDTO varianteConStock(Long id, String categoria, int stockActual, int stockMinimo, StockStatus status) {
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
                stockActual,
                stockMinimo,
                status
        );
    }
}
