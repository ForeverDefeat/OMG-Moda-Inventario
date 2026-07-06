package com.omgmoda.sistema_inventario.reportes.infraestructura.controllers;

import com.omgmoda.sistema_inventario.reportes.aplicacion.dto.ReporteResumenDTO;
import com.omgmoda.sistema_inventario.reportes.aplicacion.usecases.ReportesDownloadExportService;
import com.omgmoda.sistema_inventario.reportes.aplicacion.usecases.ReportesExcelExportService;
import com.omgmoda.sistema_inventario.reportes.aplicacion.usecases.ReportesQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReporteRestControllerTest {

    private final ReportesQueryService service = mock(ReportesQueryService.class);
    private final ReportesExcelExportService excelExportService = mock(ReportesExcelExportService.class);
    private final ReportesDownloadExportService downloadExportService = mock(ReportesDownloadExportService.class);
    private final ReporteRestController controller = new ReporteRestController(
            service,
            excelExportService,
            downloadExportService
    );

    @Test
    void resumenDelegarAlServicio() {
        when(service.obtenerResumen(null, null)).thenReturn(resumen(BigDecimal.ZERO, 0, "Sin datos", "Sin datos"));

        var response = controller.resumen(null, null, null);

        assertThat(response.getBody().reportesActivos()).isEqualTo(2);
        verify(service).obtenerResumen(null, null);
    }

    @Test
    void rotacionDelegarCategoriaYMeses() {
        when(service.rotacion("Camisas", 6)).thenReturn(List.of());

        controller.rotacion(null, "Camisas", null, null, 6);

        verify(service).rotacion("Camisas", 6);
    }

    @Test
    void ventasCategoriaConPeriodoDelegarAlServicioPorPeriodo() {
        when(service.ventasCategoria("7d")).thenReturn(List.of());

        controller.ventasCategoria("7d", null, null);

        verify(service).ventasCategoria("7d");
    }

    @Test
    void rotacionConPeriodoDelegarAlServicioPorPeriodo() {
        when(service.rotacionPorPeriodo("Camisas", "today")).thenReturn(List.of());

        controller.rotacion("today", "Camisas", null, null, 6);

        verify(service).rotacionPorPeriodo("Camisas", "today");
    }

    @Test
    void ventasTendenciaConRangoDelegarAlServicioPorFechas() {
        LocalDateTime desde = LocalDateTime.of(2026, 7, 1, 0, 0);
        LocalDateTime hasta = LocalDateTime.of(2026, 7, 31, 23, 59);
        when(service.ventasTendencia(desde, hasta)).thenReturn(List.of());

        controller.ventasTendencia(null, desde, hasta);

        verify(service).ventasTendencia(desde, hasta);
    }

    @Test
    void rotacionConRangoDelegarAlServicioPorFechas() {
        LocalDateTime desde = LocalDateTime.of(2026, 7, 1, 0, 0);
        LocalDateTime hasta = LocalDateTime.of(2026, 7, 31, 23, 59);
        when(service.rotacion(desde, hasta, "Camisas")).thenReturn(List.of());

        controller.rotacion(null, "Camisas", desde, hasta, 6);

        verify(service).rotacion(desde, hasta, "Camisas");
    }

    @Test
    void exportarInventarioRetornaArchivoExcel() {
        when(excelExportService.exportarInventarioAlertas()).thenReturn(new byte[] {1, 2, 3});

        var response = controller.exportarInventario();

        assertThat(response.getHeaders().getContentDisposition().getFilename())
                .isEqualTo("clothwise-alertas-inventario.xlsx");
        assertThat(response.getBody()).containsExactly(1, 2, 3);
    }

    @Test
    void resumenConPeriodoDelegarAlServicioPorPeriodo() {
        when(service.obtenerResumen("today")).thenReturn(resumen(BigDecimal.TEN, 2, "Camisas", "Camisa Oxford"));

        var response = controller.resumen("today", null, null);

        assertThat(response.getBody().ventasMes()).isEqualByComparingTo(BigDecimal.TEN);
        verify(service).obtenerResumen("today");
    }

    @Test
    void exportarCsvUsaNombreDescargable() {
        when(downloadExportService.exportarCsv("resumen", "7d", null, null)).thenReturn(new byte[] {9});
        when(downloadExportService.filename("resumen", "7d", "csv", null, null)).thenReturn("reporte-resumen-7d.csv");

        var response = controller.exportarCsv("resumen", "7d", null, null);

        assertThat(response.getHeaders().getContentDisposition().getFilename())
                .isEqualTo("reporte-resumen-7d.csv");
        assertThat(response.getBody()).containsExactly(9);
    }

    @Test
    void exportarPdfUsaNombreDescargable() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin@omgmoda.com");
        when(downloadExportService.exportarPdf("resumen", "30d", null, null, "admin@omgmoda.com"))
                .thenReturn(new byte[] {37, 80, 68, 70});
        when(downloadExportService.filename("resumen", "30d", "pdf", null, null)).thenReturn("reporte-resumen-30d.pdf");

        var response = controller.exportarPdf("resumen", "30d", null, null, authentication);

        assertThat(response.getHeaders().getContentDisposition().getFilename())
                .isEqualTo("reporte-resumen-30d.pdf");
        assertThat(response.getBody()).containsExactly(37, 80, 68, 70);
    }

    @Test
    void exportarCsvConRangoUsaNombreDescargable() {
        LocalDateTime desde = LocalDateTime.of(2026, 7, 1, 0, 0);
        LocalDateTime hasta = LocalDateTime.of(2026, 7, 31, 23, 59);
        when(downloadExportService.exportarCsv("rotacion", "7d", desde, hasta)).thenReturn(new byte[] {4});
        when(downloadExportService.filename("rotacion", "7d", "csv", desde, hasta))
                .thenReturn("reporte-rotacion-20260701-20260731.csv");

        var response = controller.exportarCsv("rotacion", "7d", desde, hasta);

        assertThat(response.getHeaders().getContentDisposition().getFilename())
                .isEqualTo("reporte-rotacion-20260701-20260731.csv");
        assertThat(response.getBody()).containsExactly(4);
    }

    private ReporteResumenDTO resumen(BigDecimal ventas, int unidades, String categoria, String producto) {
        return new ReporteResumenDTO(
                ventas,
                BigDecimal.ZERO,
                1,
                2,
                unidades,
                BigDecimal.valueOf(50),
                categoria,
                producto
        );
    }
}
