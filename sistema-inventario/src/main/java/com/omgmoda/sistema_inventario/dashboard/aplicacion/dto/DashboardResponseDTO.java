package com.omgmoda.sistema_inventario.dashboard.aplicacion.dto;

import com.omgmoda.sistema_inventario.compra.aplicacion.dto.CompraSugerenciaDTO;
import com.omgmoda.sistema_inventario.reportes.aplicacion.dto.MetricDatumDTO;

import java.util.List;

public record DashboardResponseDTO(
        DashboardKpisDTO kpis,
        List<MetricDatumDTO> ventasTendencia,
        List<MetricDatumDTO> ventasCategoria,
        List<MetricDatumDTO> rotacion,
        List<CompraSugerenciaDTO> sugerencias
) {}
