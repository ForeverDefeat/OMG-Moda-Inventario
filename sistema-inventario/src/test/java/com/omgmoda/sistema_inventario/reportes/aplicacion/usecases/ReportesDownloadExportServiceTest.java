package com.omgmoda.sistema_inventario.reportes.aplicacion.usecases;

import com.omgmoda.sistema_inventario.reportes.aplicacion.dto.MetricDatumDTO;
import com.omgmoda.sistema_inventario.reportes.aplicacion.dto.ReporteResumenDTO;
import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;
import com.omgmoda.sistema_inventario.shared.dominio.valueobjects.StockStatus;
import com.omgmoda.sistema_inventario.producto.aplicacion.dto.VarianteResponseDTO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportesDownloadExportServiceTest {

    private final ReportesQueryService reportesQueryService = mock(ReportesQueryService.class);
    private final ReportesDownloadExportService service = new ReportesDownloadExportService(reportesQueryService);

    @Test
    void exportaResumenCsvPorPeriodo() {
        when(reportesQueryService.obtenerResumen("today"))
                .thenReturn(resumen(BigDecimal.valueOf(120), 3, "Camisas", "Camisa Oxford"));

        String csv = new String(service.exportarCsv("resumen", "today"), StandardCharsets.UTF_8);

        assertThat(csv).contains("Indicador", "Ventas del periodo", "120", "Ticket promedio", "Camisa Oxford");
    }

    @Test
    void exportaRotacionCsvConEscapado() {
        when(reportesQueryService.rotacionPorPeriodo(null, "7d"))
                .thenReturn(List.of(new MetricDatumDTO("Camisas, Polos", BigDecimal.valueOf(250))));

        String csv = new String(service.exportarCsv("rotacion", "7d"), StandardCharsets.UTF_8);

        assertThat(csv).contains("\"Camisas, Polos\"");
        assertThat(csv).contains("\"250\"");
    }

    @Test
    void exportaRotacionPdfValido() {
        stubProfessionalReport();

        byte[] pdf = service.exportarPdf("rotacion", "30d");

        assertThat(new String(pdf, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }

    @Test
    void exportaResumenPdfProfesionalConSeccionesClave() throws Exception {
        stubProfessionalReport();

        byte[] pdf = service.exportarPdf("resumen", "7d", null, null, "admin@omgmoda.com");
        String text = pdfText(pdf);

        assertThat(text).contains(
                "OMG MODA",
                "Resumen Ejecutivo de Ventas",
                "Ventas del periodo",
                "Ticket promedio",
                "Resumen ejecutivo",
                "Ventas por categoria",
                "Productos mas vendidos",
                "admin@omgmoda.com"
        );
    }

    @Test
    void exportaRotacionPdfConAlertasYRecomendaciones() throws Exception {
        stubProfessionalReport();

        byte[] pdf = service.exportarPdf("rotacion", "7d", null, null, "admin@omgmoda.com");
        String text = pdfText(pdf);

        assertThat(text).contains(
                "Reporte de Rotacion de Inventario",
                "SKUs con alerta",
                "Productos con baja rotacion",
                "Recomendaciones",
                "Reponer productos criticos"
        );
    }

    @Test
    void exportaResumenCsvPorRango() {
        LocalDateTime desde = LocalDateTime.of(2026, 7, 1, 0, 0);
        LocalDateTime hasta = LocalDateTime.of(2026, 7, 31, 23, 59);
        when(reportesQueryService.obtenerResumen(desde, hasta))
                .thenReturn(resumen(BigDecimal.valueOf(500), 8, "Vestidos", "Vestido Midi"));

        String csv = new String(service.exportarCsv("resumen", "7d", desde, hasta), StandardCharsets.UTF_8);

        assertThat(csv).contains("Vestidos", "Vestido Midi");
    }

    @Test
    void rechazaTiposRetiradosDeDescarga() {
        assertThatThrownBy(() -> service.exportarCsv("ventas-categoria", "7d"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Tipo de reporte no soportado");
        assertThatThrownBy(() -> service.exportarPdf("stock-alertas", "7d"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Tipo de reporte no soportado");
    }

    @Test
    void rechazaTipoDesconocido() {
        assertThatThrownBy(() -> service.exportarCsv("desconocido", "7d"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Tipo de reporte no soportado");
    }

    @Test
    void generaNombreConPeriodoNormalizado() {
        assertThat(service.filename("resumen", "today", "pdf")).isEqualTo("reporte-resumen-today.pdf");
        assertThat(service.filename("resumen", "invalido", "csv")).isEqualTo("reporte-resumen-7d.csv");
    }

    @Test
    void generaNombreConRango() {
        LocalDateTime desde = LocalDateTime.of(2026, 7, 1, 0, 0);
        LocalDateTime hasta = LocalDateTime.of(2026, 7, 31, 23, 59);

        assertThat(service.filename("rotacion", "7d", "csv", desde, hasta))
                .isEqualTo("reporte-rotacion-20260701-20260731.csv");
    }

    private ReporteResumenDTO resumen(BigDecimal ventas, int unidades, String categoria, String producto) {
        return new ReporteResumenDTO(
                ventas,
                BigDecimal.valueOf(10),
                2,
                2,
                unidades,
                BigDecimal.valueOf(60),
                categoria,
                producto
        );
    }

    private void stubProfessionalReport() {
        when(reportesQueryService.rangoPorPeriodo(any()))
                .thenReturn(new ReportesQueryService.Rango(
                        LocalDateTime.of(2026, 6, 1, 0, 0),
                        LocalDateTime.of(2026, 6, 30, 23, 59)
                ));
        when(reportesQueryService.obtenerResumen(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(resumen(BigDecimal.valueOf(4149.60), 34, "Sacos", "Panuelo Floral"));
        when(reportesQueryService.ventasTendencia(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(
                        new MetricDatumDTO("Lun", BigDecimal.valueOf(800)),
                        new MetricDatumDTO("Mar", BigDecimal.valueOf(1200)),
                        new MetricDatumDTO("Mie", BigDecimal.valueOf(2149.60))
                ));
        when(reportesQueryService.ventasCategoriaDetalle(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(
                        new ReportesQueryService.CategoriaVentaDTO("Sacos", BigDecimal.valueOf(2500), 20, BigDecimal.valueOf(60.25)),
                        new ReportesQueryService.CategoriaVentaDTO("Blusas", BigDecimal.valueOf(1649.60), 14, BigDecimal.valueOf(39.75))
                ));
        when(reportesQueryService.productosMasVendidos(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(new ReportesQueryService.ProductoVentaDTO("Panuelo Floral", "Sacos", 12, BigDecimal.valueOf(1200))));
        when(reportesQueryService.rotacion(any(LocalDateTime.class), any(LocalDateTime.class), any(String.class)))
                .thenReturn(List.of(new MetricDatumDTO("Sacos", BigDecimal.valueOf(20)), new MetricDatumDTO("Blusas", BigDecimal.valueOf(14))));
        when(reportesQueryService.stockAlertas()).thenReturn(List.of(variante()));
        when(reportesQueryService.productosBajaRotacion(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(new ReportesQueryService.ProductoRotacionDTO("SKU-99", "Abrigo Lino", "Abrigos", 0, 18, 4, "NORMAL")));
    }

    private VarianteResponseDTO variante() {
        return new VarianteResponseDTO(
                10L,
                1L,
                "OMG-SACO-01",
                "Saco Verde",
                "Sacos",
                "OMG MODA",
                null,
                "M",
                "Verde",
                "Lino",
                BigDecimal.valueOf(80),
                BigDecimal.valueOf(160),
                2,
                5,
                StockStatus.BAJO_STOCK
        );
    }

    private String pdfText(byte[] pdf) throws IOException {
        try (var document = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(document);
        }
    }
}
