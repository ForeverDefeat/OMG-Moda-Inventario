package com.omgmoda.sistema_inventario.reportes.infraestructura.controllers;

import com.omgmoda.sistema_inventario.producto.aplicacion.dto.VarianteResponseDTO;
import com.omgmoda.sistema_inventario.reportes.aplicacion.dto.MetricDatumDTO;
import com.omgmoda.sistema_inventario.reportes.aplicacion.dto.ReporteResumenDTO;
import com.omgmoda.sistema_inventario.reportes.aplicacion.usecases.ReportesQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reportes")
@Tag(name = "Reportes", description = "Consultas agregadas de ventas, inventario y rotacion.")
@SecurityRequirement(name = "bearer-jwt")
public class ReporteRestController {

    private final ReportesQueryService reportesQueryService;

    public ReporteRestController(ReportesQueryService reportesQueryService) {
        this.reportesQueryService = reportesQueryService;
    }

    @GetMapping("/resumen")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Resumen de reportes", description = "Retorna KPIs principales del rango solicitado.")
    public ResponseEntity<ReporteResumenDTO> resumen(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(reportesQueryService.obtenerResumen(desde, hasta));
    }

    @GetMapping("/ventas-tendencia")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tendencia de ventas", description = "Retorna ventas agrupadas por hora o dia.")
    public ResponseEntity<List<MetricDatumDTO>> ventasTendencia(
            @RequestParam(defaultValue = "7d") String periodo) {
        return ResponseEntity.ok(reportesQueryService.ventasTendencia(periodo));
    }

    @GetMapping("/ventas-categoria")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ventas por categoria", description = "Retorna ventas completadas agrupadas por categoria.")
    public ResponseEntity<List<MetricDatumDTO>> ventasCategoria(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(reportesQueryService.ventasCategoria(desde, hasta));
    }

    @GetMapping("/rotacion")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Rotacion de inventario", description = "Retorna unidades vendidas por mes.")
    public ResponseEntity<List<MetricDatumDTO>> rotacion(
            @RequestParam(required = false) String categoria,
            @RequestParam(defaultValue = "6") int meses) {
        return ResponseEntity.ok(reportesQueryService.rotacion(categoria, meses));
    }

    @GetMapping("/stock-alertas")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Alertas de stock", description = "Retorna variantes en o por debajo del stock minimo.")
    public ResponseEntity<List<VarianteResponseDTO>> stockAlertas() {
        return ResponseEntity.ok(reportesQueryService.stockAlertas());
    }
}
