package com.omgmoda.sistema_inventario.dashboard.aplicacion.usecases;

import com.omgmoda.sistema_inventario.compra.aplicacion.dto.CompraSugerenciaDTO;
import com.omgmoda.sistema_inventario.compra.aplicacion.usecases.CompraSugerenciaService;
import com.omgmoda.sistema_inventario.dashboard.aplicacion.dto.DashboardKpisDTO;
import com.omgmoda.sistema_inventario.dashboard.aplicacion.dto.DashboardResponseDTO;
import com.omgmoda.sistema_inventario.producto.aplicacion.dto.VarianteResponseDTO;
import com.omgmoda.sistema_inventario.producto.aplicacion.ports.IBuscarVariantesUseCase;
import com.omgmoda.sistema_inventario.reportes.aplicacion.dto.MetricDatumDTO;
import com.omgmoda.sistema_inventario.reportes.aplicacion.usecases.ReportesQueryService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DashboardQueryService {

    private final ReportesQueryService reportesQueryService;
    private final CompraSugerenciaService compraSugerenciaService;
    private final IBuscarVariantesUseCase buscarVariantesUseCase;

    public DashboardQueryService(ReportesQueryService reportesQueryService,
                                 CompraSugerenciaService compraSugerenciaService,
                                 IBuscarVariantesUseCase buscarVariantesUseCase) {
        this.reportesQueryService = reportesQueryService;
        this.compraSugerenciaService = compraSugerenciaService;
        this.buscarVariantesUseCase = buscarVariantesUseCase;
    }

    public DashboardResponseDTO obtenerDashboard(String periodo, String canal, String categoria) {
        List<VarianteResponseDTO> variantes = buscarVariantesUseCase.buscar(null, null, null);
        List<CompraSugerenciaDTO> sugerencias = compraSugerenciaService.listarSugerencias();
        boolean canalSinDatos = "online".equalsIgnoreCase(canal == null ? "" : canal);

        List<MetricDatumDTO> tendencia = canalSinDatos ? List.of() : reportesQueryService.ventasTendencia(periodo);
        ReportesQueryService.Rango rango = reportesQueryService.rangoPorPeriodo(periodo);
        List<MetricDatumDTO> categorias = canalSinDatos
                ? List.of()
                : reportesQueryService.ventasCategoria(rango.desde(), rango.hasta());
        List<MetricDatumDTO> rotacion = canalSinDatos ? List.of() : reportesQueryService.rotacion(categoria, 6);
        BigDecimal ventasPeriodo = tendencia.stream()
                .map(MetricDatumDTO::value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        DashboardKpisDTO kpis = new DashboardKpisDTO(
                valorStock(variantes),
                variantes.size(),
                (int) variantes.stream().filter(v -> v.stockActual() <= v.stockMinimo()).count(),
                ventasPeriodo,
                sugerencias.size()
        );

        return new DashboardResponseDTO(
                kpis,
                tendencia,
                categorias,
                rotacion,
                sugerencias.stream().limit(3).toList()
        );
    }

    private BigDecimal valorStock(List<VarianteResponseDTO> variantes) {
        return variantes.stream()
                .map(v -> v.precioCosto().multiply(BigDecimal.valueOf(v.stockActual())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
