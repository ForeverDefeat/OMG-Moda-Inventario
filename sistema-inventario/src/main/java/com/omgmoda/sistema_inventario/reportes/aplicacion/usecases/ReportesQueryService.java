package com.omgmoda.sistema_inventario.reportes.aplicacion.usecases;

import com.omgmoda.sistema_inventario.producto.aplicacion.dto.VarianteResponseDTO;
import com.omgmoda.sistema_inventario.producto.aplicacion.ports.IBuscarVariantesUseCase;
import com.omgmoda.sistema_inventario.reportes.aplicacion.dto.MetricDatumDTO;
import com.omgmoda.sistema_inventario.reportes.aplicacion.dto.ReporteResumenDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.VentaResponseDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.ports.IConsultarVentaUseCase;
import com.omgmoda.sistema_inventario.venta.dominio.EstadoVenta;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReportesQueryService {

    private static final Locale LOCALE_ES = Locale.forLanguageTag("es-PE");

    private final IConsultarVentaUseCase consultarVentaUseCase;
    private final IBuscarVariantesUseCase buscarVariantesUseCase;

    public ReportesQueryService(IConsultarVentaUseCase consultarVentaUseCase,
                                IBuscarVariantesUseCase buscarVariantesUseCase) {
        this.consultarVentaUseCase = consultarVentaUseCase;
        this.buscarVariantesUseCase = buscarVariantesUseCase;
    }

    public ReporteResumenDTO obtenerResumen(LocalDateTime desde, LocalDateTime hasta) {
        Rango rango = normalizarRango(desde, hasta);
        BigDecimal ventasActuales = totalVentas(rango.desde(), rango.hasta());
        long dias = Math.max(1, java.time.Duration.between(rango.desde(), rango.hasta()).toDays() + 1);
        LocalDateTime previoHasta = rango.desde().minusNanos(1);
        LocalDateTime previoDesde = previoHasta.minusDays(dias - 1).toLocalDate().atStartOfDay();
        BigDecimal ventasPrevias = totalVentas(previoDesde, previoHasta);

        BigDecimal crecimiento = BigDecimal.ZERO;
        if (ventasPrevias.signum() > 0) {
            crecimiento = ventasActuales.subtract(ventasPrevias)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(ventasPrevias, 2, RoundingMode.HALF_UP);
        } else if (ventasActuales.signum() > 0) {
            crecimiento = BigDecimal.valueOf(100);
        }

        int alertas = stockAlertas().size();
        return new ReporteResumenDTO(ventasActuales, crecimiento, alertas, 5);
    }

    public List<MetricDatumDTO> ventasTendencia(String periodo) {
        Rango rango = rangoPorPeriodo(periodo);
        List<VentaResponseDTO> ventas = ventasCompletadas(rango.desde(), rango.hasta());
        if (ventas.isEmpty()) return List.of();

        String normalized = periodo == null ? "7d" : periodo.toLowerCase(Locale.ROOT);
        if (normalized.equals("today")) {
            Map<Integer, BigDecimal> porHora = ventas.stream()
                    .collect(Collectors.groupingBy(
                            venta -> venta.fecha().getHour(),
                            LinkedHashMap::new,
                            Collectors.reducing(BigDecimal.ZERO, VentaResponseDTO::total, BigDecimal::add)
                    ));
            return porHora.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> new MetricDatumDTO(String.format("%02dh", entry.getKey()), entry.getValue()))
                    .toList();
        }

        Map<LocalDate, BigDecimal> porDia = ventas.stream()
                .collect(Collectors.groupingBy(
                        venta -> venta.fecha().toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, VentaResponseDTO::total, BigDecimal::add)
                ));
        return porDia.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new MetricDatumDTO(labelDia(entry.getKey(), normalized), entry.getValue()))
                .toList();
    }

    public List<MetricDatumDTO> ventasCategoria(LocalDateTime desde, LocalDateTime hasta) {
        Rango rango = normalizarRango(desde, hasta);
        List<VentaResponseDTO> ventas = ventasCompletadas(rango.desde(), rango.hasta());
        if (ventas.isEmpty()) return List.of();

        Map<Long, VarianteResponseDTO> variantes = variantesPorId();
        Map<String, BigDecimal> totalPorCategoria = new LinkedHashMap<>();
        for (VentaResponseDTO venta : ventas) {
            for (VentaResponseDTO.DetalleVentaResponseDTO detalle : venta.detalles()) {
                VarianteResponseDTO variante = variantes.get(detalle.idVariante());
                if (variante == null) continue;
                totalPorCategoria.merge(variante.categoria(), detalle.subtotal(), BigDecimal::add);
            }
        }

        return totalPorCategoria.entrySet()
                .stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .map(entry -> new MetricDatumDTO(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<MetricDatumDTO> rotacion(String categoria, int meses) {
        int mesesNormalizados = Math.max(1, Math.min(meses <= 0 ? 6 : meses, 12));
        LocalDate primerDia = YearMonth.now().minusMonths(mesesNormalizados - 1).atDay(1);
        LocalDateTime desde = primerDia.atStartOfDay();
        LocalDateTime hasta = LocalDateTime.now();
        List<VentaResponseDTO> ventas = ventasCompletadas(desde, hasta);
        if (ventas.isEmpty()) return List.of();

        Map<Long, VarianteResponseDTO> variantes = variantesPorId();
        Map<YearMonth, BigDecimal> unidadesPorMes = new LinkedHashMap<>();
        for (int i = mesesNormalizados - 1; i >= 0; i--) {
            unidadesPorMes.put(YearMonth.now().minusMonths(i), BigDecimal.ZERO);
        }

        for (VentaResponseDTO venta : ventas) {
            YearMonth mes = YearMonth.from(venta.fecha());
            for (VentaResponseDTO.DetalleVentaResponseDTO detalle : venta.detalles()) {
                VarianteResponseDTO variante = variantes.get(detalle.idVariante());
                if (variante == null) continue;
                if (categoria != null && !categoria.isBlank() && !categoria.equalsIgnoreCase("Todas")
                        && !variante.categoria().equalsIgnoreCase(categoria)) {
                    continue;
                }
                unidadesPorMes.merge(mes, BigDecimal.valueOf(detalle.cantidad()), BigDecimal::add);
            }
        }

        return unidadesPorMes.entrySet()
                .stream()
                .filter(entry -> entry.getValue().signum() > 0 || ventas.size() > 0)
                .map(entry -> new MetricDatumDTO(labelMes(entry.getKey()), entry.getValue()))
                .toList();
    }

    public List<VarianteResponseDTO> stockAlertas() {
        return buscarVariantesUseCase.buscarBajoStock();
    }

    public BigDecimal totalVentasPeriodo(String periodo) {
        Rango rango = rangoPorPeriodo(periodo);
        return totalVentas(rango.desde(), rango.hasta());
    }

    public Rango rangoPorPeriodo(String periodo) {
        String normalized = periodo == null ? "7d" : periodo.toLowerCase(Locale.ROOT);
        LocalDate hoy = LocalDate.now();
        LocalDateTime ahora = LocalDateTime.now();
        return switch (normalized) {
            case "today" -> new Rango(hoy.atStartOfDay(), ahora);
            case "30d" -> new Rango(hoy.minusDays(29).atStartOfDay(), ahora);
            default -> new Rango(hoy.minusDays(6).atStartOfDay(), ahora);
        };
    }

    public record Rango(LocalDateTime desde, LocalDateTime hasta) {}

    private Rango normalizarRango(LocalDateTime desde, LocalDateTime hasta) {
        if (desde != null && hasta != null) return new Rango(desde, hasta);
        return rangoPorPeriodo("30d");
    }

    private BigDecimal totalVentas(LocalDateTime desde, LocalDateTime hasta) {
        return ventasCompletadas(desde, hasta)
                .stream()
                .map(VentaResponseDTO::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<VentaResponseDTO> ventasCompletadas(LocalDateTime desde, LocalDateTime hasta) {
        return consultarVentaUseCase.buscarPorFechas(desde, hasta)
                .stream()
                .filter(venta -> venta.estado() == EstadoVenta.COMPLETADA)
                .toList();
    }

    private Map<Long, VarianteResponseDTO> variantesPorId() {
        return buscarVariantesUseCase.buscar(null, null, null)
                .stream()
                .collect(Collectors.toMap(
                        VarianteResponseDTO::idVariante,
                        Function.identity(),
                        (left, right) -> left
                ));
    }

    private String labelDia(LocalDate fecha, String periodo) {
        if (periodo.equals("30d")) {
            return String.format("%02d/%02d", fecha.getDayOfMonth(), fecha.getMonthValue());
        }
        return capitalizar(fecha.getDayOfWeek().getDisplayName(TextStyle.SHORT, LOCALE_ES));
    }

    private String labelMes(YearMonth mes) {
        return capitalizar(mes.getMonth().getDisplayName(TextStyle.SHORT, LOCALE_ES));
    }

    private String capitalizar(String value) {
        String limpio = value.replace(".", "");
        if (limpio.isBlank()) return limpio;
        return limpio.substring(0, 1).toUpperCase(LOCALE_ES) + limpio.substring(1);
    }
}
