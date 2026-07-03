package com.omgmoda.sistema_inventario.dashboard.infraestructura.controllers;

import com.omgmoda.sistema_inventario.dashboard.aplicacion.dto.DashboardResponseDTO;
import com.omgmoda.sistema_inventario.dashboard.aplicacion.usecases.DashboardQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Vista compuesta para panel principal.")
@SecurityRequirement(name = "bearer-jwt")
public class DashboardRestController {

    private final DashboardQueryService dashboardQueryService;

    public DashboardRestController(DashboardQueryService dashboardQueryService) {
        this.dashboardQueryService = dashboardQueryService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @Operation(summary = "Obtener dashboard", description = "Retorna KPIs, graficos y sugerencias desde datos reales.")
    public ResponseEntity<DashboardResponseDTO> obtenerDashboard(
            @RequestParam(defaultValue = "today") String periodo,
            @RequestParam(defaultValue = "all") String canal,
            @RequestParam(required = false) String categoria) {
        return ResponseEntity.ok(dashboardQueryService.obtenerDashboard(periodo, canal, categoria));
    }
}
