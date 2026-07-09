package com.omgmoda.sistema_inventario.reportes.infraestructura.controllers;

import com.omgmoda.sistema_inventario.producto.aplicacion.dto.VarianteResponseDTO;
import com.omgmoda.sistema_inventario.reportes.aplicacion.dto.MetricDatumDTO;
import com.omgmoda.sistema_inventario.reportes.aplicacion.dto.ReporteResumenDTO;
import com.omgmoda.sistema_inventario.reportes.aplicacion.usecases.ReportesDownloadExportService;
import com.omgmoda.sistema_inventario.reportes.aplicacion.usecases.ReportesExcelExportService;
import com.omgmoda.sistema_inventario.reportes.aplicacion.usecases.ReportesQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Adaptador REST para exponer las operaciones de ReporteRestController mediante endpoints HTTP.
 */
@RestController
@RequestMapping("/api/v1/reportes")
@Tag(name = "Reportes", description = "Consultas agregadas de ventas, inventario y rotacion.")
@SecurityRequirement(name = "bearer-jwt")
public class ReporteRestController {

    private final ReportesQueryService reportesQueryService;
    private final ReportesExcelExportService reportesExcelExportService;
    private final ReportesDownloadExportService reportesDownloadExportService;

    public ReporteRestController(ReportesQueryService reportesQueryService,
                                 ReportesExcelExportService reportesExcelExportService,
                                 ReportesDownloadExportService reportesDownloadExportService) {
        this.reportesQueryService = reportesQueryService;
        this.reportesExcelExportService = reportesExcelExportService;
        this.reportesDownloadExportService = reportesDownloadExportService;
    }

    @GetMapping("/resumen")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Resumen de reportes", description = "Retorna KPIs principales del rango solicitado.")
    public ResponseEntity<ReporteResumenDTO> resumen(
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        if (periodo != null && !periodo.isBlank()) {
            return ResponseEntity.ok(reportesQueryService.obtenerResumen(periodo));
        }
        return ResponseEntity.ok(reportesQueryService.obtenerResumen(desde, hasta));
    }

    @GetMapping("/ventas-tendencia")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tendencia de ventas", description = "Retorna ventas agrupadas por hora o dia.")
    public ResponseEntity<List<MetricDatumDTO>> ventasTendencia(
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        if (desde != null && hasta != null) {
            return ResponseEntity.ok(reportesQueryService.ventasTendencia(desde, hasta));
        }
        if (periodo == null || periodo.isBlank()) {
            periodo = "7d";
        }
        return ResponseEntity.ok(reportesQueryService.ventasTendencia(periodo));
    }

    @GetMapping("/ventas-categoria")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ventas por categoria", description = "Retorna ventas completadas agrupadas por categoria.")
    public ResponseEntity<List<MetricDatumDTO>> ventasCategoria(
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        if (periodo != null && !periodo.isBlank()) {
            return ResponseEntity.ok(reportesQueryService.ventasCategoria(periodo));
        }
        return ResponseEntity.ok(reportesQueryService.ventasCategoria(desde, hasta));
    }

    @GetMapping("/rotacion")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Rotacion de inventario", description = "Retorna unidades vendidas por mes.")
    public ResponseEntity<List<MetricDatumDTO>> rotacion(
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(defaultValue = "6") int meses) {
        if (periodo != null && !periodo.isBlank()) {
            return ResponseEntity.ok(reportesQueryService.rotacionPorPeriodo(categoria, periodo));
        }
        if (desde != null && hasta != null) {
            return ResponseEntity.ok(reportesQueryService.rotacion(desde, hasta, categoria));
        }
        return ResponseEntity.ok(reportesQueryService.rotacion(categoria, meses));
    }

    @GetMapping("/stock-alertas")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Alertas de stock", description = "Retorna variantes en o por debajo del stock minimo.")
    public ResponseEntity<List<VarianteResponseDTO>> stockAlertas() {
        return ResponseEntity.ok(reportesQueryService.stockAlertas());
    }

    @GetMapping("/export/inventario")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Exportar alertas de inventario", description = "Genera un archivo XLSX con variantes en bajo stock.")
    public ResponseEntity<byte[]> exportarInventario() {
        byte[] workbook = reportesExcelExportService.exportarInventarioAlertas();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=clothwise-alertas-inventario.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(workbook);
    }

    @GetMapping("/export/{tipo}.csv")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Exportar reporte CSV", description = "Genera un archivo CSV para el reporte y periodo solicitados.")
    public ResponseEntity<byte[]> exportarCsv(
            @PathVariable String tipo,
            @RequestParam(defaultValue = "7d") String periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        byte[] csv = reportesDownloadExportService.exportarCsv(tipo, periodo, desde, hasta);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + reportesDownloadExportService.filename(tipo, periodo, "csv", desde, hasta))
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @GetMapping("/export/{tipo}.pdf")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Exportar reporte PDF", description = "Genera un archivo PDF ejecutivo para el reporte y periodo solicitados.")
    public ResponseEntity<byte[]> exportarPdf(
            @PathVariable String tipo,
            @RequestParam(defaultValue = "7d") String periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            Authentication authentication) {
        String usuario = authentication == null ? "No disponible" : authentication.getName();
        byte[] pdf = reportesDownloadExportService.exportarPdf(tipo, periodo, desde, hasta, usuario);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + reportesDownloadExportService.filename(tipo, periodo, "pdf", desde, hasta))
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
