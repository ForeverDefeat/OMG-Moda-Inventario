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
import java.time.temporal.ChronoUnit;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Comparator;
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
        return obtenerResumen(rango);
    }

    public ReporteResumenDTO obtenerResumen(String periodo) {
        return obtenerResumen(rangoPorPeriodo(periodo));
    }

    private ReporteResumenDTO obtenerResumen(Rango rango) {
        List<VentaResponseDTO> ventas = ventasCompletadas(rango.desde(), rango.hasta());
        BigDecimal ventasActuales = totalVentas(ventas);
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

        int unidadesVendidas = unidadesVendidas(ventas);
        BigDecimal ticketPromedio = ventas.isEmpty()
                ? BigDecimal.ZERO
                : ventasActuales.divide(BigDecimal.valueOf(ventas.size()), 2, RoundingMode.HALF_UP);
        Map<Long, VarianteResponseDTO> variantes = variantesPorId();
        int alertas = stockAlertas().size();
        return new ReporteResumenDTO(
                ventasActuales,
                crecimiento,
                alertas,
                2,
                unidadesVendidas,
                ticketPromedio,
                categoriaPrincipal(ventas, variantes),
                productoMasVendido(ventas, variantes)
        );
    }

    public List<MetricDatumDTO> ventasTendencia(String periodo) {
        Rango rango = rangoPorPeriodo(periodo);
        return ventasTendencia(rango, periodo);
    }

    public List<MetricDatumDTO> ventasTendencia(LocalDateTime desde, LocalDateTime hasta) {
        Rango rango = normalizarRango(desde, hasta);
        return ventasTendencia(rango, null);
    }

    private List<MetricDatumDTO> ventasTendencia(Rango rango, String periodo) {
        List<VentaResponseDTO> ventas = ventasCompletadas(rango.desde(), rango.hasta());
        if (ventas.isEmpty()) return List.of();

        String normalized = periodo == null ? "7d" : periodo.toLowerCase(Locale.ROOT);
        if (normalized.equals("today") || esRangoDeUnDia(rango)) {
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

        if (normalized.equals("120d") || diasDelRango(rango) > 62) {
            Map<YearMonth, BigDecimal> porMes = ventas.stream()
                    .collect(Collectors.groupingBy(
                            venta -> YearMonth.from(venta.fecha()),
                            LinkedHashMap::new,
                            Collectors.reducing(BigDecimal.ZERO, VentaResponseDTO::total, BigDecimal::add)
                    ));
            return porMes.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> new MetricDatumDTO(labelMes(entry.getKey()), entry.getValue()))
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
        return ventasCategoria(rango);
    }

    public List<MetricDatumDTO> ventasCategoria(String periodo) {
        return ventasCategoria(rangoPorPeriodo(periodo));
    }

    public List<CategoriaVentaDTO> ventasCategoriaDetalle(LocalDateTime desde, LocalDateTime hasta) {
        Rango rango = normalizarRango(desde, hasta);
        List<VentaResponseDTO> ventas = ventasCompletadas(rango.desde(), rango.hasta());
        if (ventas.isEmpty()) return List.of();

        Map<Long, VarianteResponseDTO> variantes = variantesPorId();
        Map<String, CategoriaAccumulator> acumulado = new LinkedHashMap<>();
        for (VentaResponseDTO venta : ventas) {
            for (VentaResponseDTO.DetalleVentaResponseDTO detalle : venta.detalles()) {
                VarianteResponseDTO variante = variantes.get(detalle.idVariante());
                if (variante == null) continue;
                acumulado.computeIfAbsent(variante.categoria(), key -> new CategoriaAccumulator())
                        .sumar(detalle.subtotal(), detalle.cantidad());
            }
        }

        BigDecimal total = acumulado.values().stream()
                .map(item -> item.ventas)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return acumulado.entrySet().stream()
                .sorted((left, right) -> right.getValue().ventas.compareTo(left.getValue().ventas))
                .map(entry -> new CategoriaVentaDTO(
                        entry.getKey(),
                        entry.getValue().ventas,
                        entry.getValue().unidades,
                        porcentaje(entry.getValue().ventas, total)
                ))
                .toList();
    }

    private List<MetricDatumDTO> ventasCategoria(Rango rango) {
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

    public List<MetricDatumDTO> rotacionPorPeriodo(String categoria, String periodo) {
        Rango rango = rangoPorPeriodo(periodo);
        return rotacionPorRango(categoria, rango, periodo);
    }

    public List<MetricDatumDTO> rotacion(LocalDateTime desde, LocalDateTime hasta, String categoria) {
        Rango rango = normalizarRango(desde, hasta);
        return rotacionPorRango(categoria, rango, null);
    }

    public List<ProductoVentaDTO> productosMasVendidos(LocalDateTime desde, LocalDateTime hasta) {
        Rango rango = normalizarRango(desde, hasta);
        List<VentaResponseDTO> ventas = ventasCompletadas(rango.desde(), rango.hasta());
        if (ventas.isEmpty()) return List.of();

        Map<Long, VarianteResponseDTO> variantes = variantesPorId();
        Map<String, ProductoAccumulator> acumulado = new LinkedHashMap<>();
        for (VentaResponseDTO venta : ventas) {
            for (VentaResponseDTO.DetalleVentaResponseDTO detalle : venta.detalles()) {
                VarianteResponseDTO variante = variantes.get(detalle.idVariante());
                if (variante == null) continue;
                String producto = variante.nombreProducto();
                acumulado.computeIfAbsent(producto, key -> new ProductoAccumulator(producto, variante.categoria()))
                        .sumar(detalle.subtotal(), detalle.cantidad());
            }
        }

        return acumulado.values().stream()
                .sorted(Comparator.comparingInt(ProductoAccumulator::unidades).reversed()
                        .thenComparing(ProductoAccumulator::ingresos, Comparator.reverseOrder()))
                .map(item -> new ProductoVentaDTO(item.producto, item.categoria, item.unidades, item.ingresos))
                .toList();
    }

    public List<ProductoRotacionDTO> productosBajaRotacion(LocalDateTime desde, LocalDateTime hasta) {
        Rango rango = normalizarRango(desde, hasta);
        List<VentaResponseDTO> ventas = ventasCompletadas(rango.desde(), rango.hasta());
        Map<Long, VarianteResponseDTO> variantes = variantesPorId();
        Map<Long, Integer> unidadesVendidas = new LinkedHashMap<>();

        for (VentaResponseDTO venta : ventas) {
            for (VentaResponseDTO.DetalleVentaResponseDTO detalle : venta.detalles()) {
                unidadesVendidas.merge(detalle.idVariante(), detalle.cantidad(), Integer::sum);
            }
        }

        return variantes.values().stream()
                .map(variante -> new ProductoRotacionDTO(
                        variante.sku(),
                        variante.nombreProducto(),
                        variante.categoria(),
                        unidadesVendidas.getOrDefault(variante.idVariante(), 0),
                        variante.stockActual(),
                        variante.stockMinimo(),
                        variante.stockStatus().name()
                ))
                .filter(item -> item.stockActual() > item.stockMinimo() && item.unidadesVendidas() <= 1)
                .sorted(Comparator.comparingInt(ProductoRotacionDTO::unidadesVendidas)
                        .thenComparing(ProductoRotacionDTO::stockActual, Comparator.reverseOrder()))
                .toList();
    }

    private List<MetricDatumDTO> rotacionPorRango(String categoria, Rango rango, String periodo) {
        List<VentaResponseDTO> ventas = ventasCompletadas(rango.desde(), rango.hasta());
        if (ventas.isEmpty()) return List.of();

        Map<Long, VarianteResponseDTO> variantes = variantesPorId();
        Map<String, BigDecimal> unidadesPorEtiqueta = new LinkedHashMap<>();

        for (VentaResponseDTO venta : ventas) {
            String etiqueta = etiquetaRotacionPorPeriodo(venta.fecha(), periodo, rango);
            for (VentaResponseDTO.DetalleVentaResponseDTO detalle : venta.detalles()) {
                VarianteResponseDTO variante = variantes.get(detalle.idVariante());
                if (variante == null) continue;
                if (categoria != null && !categoria.isBlank() && !categoria.equalsIgnoreCase("Todas")
                        && !variante.categoria().equalsIgnoreCase(categoria)) {
                    continue;
                }
                unidadesPorEtiqueta.merge(etiqueta, BigDecimal.valueOf(detalle.cantidad()), BigDecimal::add);
            }
        }

        return unidadesPorEtiqueta.entrySet()
                .stream()
                .map(entry -> new MetricDatumDTO(entry.getKey(), entry.getValue()))
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
            case "120d" -> new Rango(hoy.minusDays(119).atStartOfDay(), ahora);
            case "30d" -> new Rango(hoy.minusDays(29).atStartOfDay(), ahora);
            default -> new Rango(hoy.minusDays(6).atStartOfDay(), ahora);
        };
    }

    public record Rango(LocalDateTime desde, LocalDateTime hasta) {}

    public record CategoriaVentaDTO(String categoria,
                                    BigDecimal ventas,
                                    int unidadesVendidas,
                                    BigDecimal participacionPorcentaje) {}

    public record ProductoVentaDTO(String producto,
                                   String categoria,
                                   int unidadesVendidas,
                                   BigDecimal ingresos) {}

    public record ProductoRotacionDTO(String sku,
                                      String producto,
                                      String categoria,
                                      int unidadesVendidas,
                                      int stockActual,
                                      int stockMinimo,
                                      String estado) {}

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

    private BigDecimal totalVentas(List<VentaResponseDTO> ventas) {
        return ventas.stream()
                .map(VentaResponseDTO::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal porcentaje(BigDecimal valor, BigDecimal total) {
        if (total.signum() == 0) return BigDecimal.ZERO;
        return valor.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
    }

    private int unidadesVendidas(List<VentaResponseDTO> ventas) {
        return ventas.stream()
                .flatMap(venta -> venta.detalles().stream())
                .mapToInt(VentaResponseDTO.DetalleVentaResponseDTO::cantidad)
                .sum();
    }

    private String categoriaPrincipal(List<VentaResponseDTO> ventas, Map<Long, VarianteResponseDTO> variantes) {
        Map<String, BigDecimal> totales = new LinkedHashMap<>();
        for (VentaResponseDTO venta : ventas) {
            for (VentaResponseDTO.DetalleVentaResponseDTO detalle : venta.detalles()) {
                VarianteResponseDTO variante = variantes.get(detalle.idVariante());
                if (variante == null) continue;
                totales.merge(variante.categoria(), detalle.subtotal(), BigDecimal::add);
            }
        }
        return totales.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Sin datos");
    }

    private String productoMasVendido(List<VentaResponseDTO> ventas, Map<Long, VarianteResponseDTO> variantes) {
        Map<String, Integer> unidades = new LinkedHashMap<>();
        for (VentaResponseDTO venta : ventas) {
            for (VentaResponseDTO.DetalleVentaResponseDTO detalle : venta.detalles()) {
                VarianteResponseDTO variante = variantes.get(detalle.idVariante());
                if (variante == null) continue;
                unidades.merge(variante.nombreProducto(), detalle.cantidad(), Integer::sum);
            }
        }
        return unidades.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Sin datos");
    }

    private List<VentaResponseDTO> ventasCompletadas(LocalDateTime desde, LocalDateTime hasta) {
        return consultarVentaUseCase.buscarPorFechas(desde, hasta)
                .stream()
                .filter(venta -> venta.estado().normalizado() == EstadoVenta.COMPLETED)
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
        if (periodo.equals("30d") || periodo.equals("120d")) {
            return String.format("%02d/%02d", fecha.getDayOfMonth(), fecha.getMonthValue());
        }
        return capitalizar(fecha.getDayOfWeek().getDisplayName(TextStyle.SHORT, LOCALE_ES));
    }

    private String labelMes(YearMonth mes) {
        return capitalizar(mes.getMonth().getDisplayName(TextStyle.SHORT, LOCALE_ES));
    }

    private String etiquetaRotacionPorPeriodo(LocalDateTime fecha, String periodo, Rango rango) {
        String normalized = periodo == null ? "7d" : periodo.toLowerCase(Locale.ROOT);
        if (normalized.equals("today") || esRangoDeUnDia(rango)) {
            return String.format("%02dh", fecha.getHour());
        }
        if (normalized.equals("30d") || normalized.equals("120d") || diasDelRango(rango) > 62) {
            return labelMes(YearMonth.from(fecha));
        }
        return labelDia(fecha.toLocalDate(), normalized);
    }

    private boolean esRangoDeUnDia(Rango rango) {
        return rango.desde().toLocalDate().equals(rango.hasta().toLocalDate());
    }

    private long diasDelRango(Rango rango) {
        return ChronoUnit.DAYS.between(rango.desde().toLocalDate(), rango.hasta().toLocalDate()) + 1;
    }

    private String capitalizar(String value) {
        String limpio = value.replace(".", "");
        if (limpio.isBlank()) return limpio;
        return limpio.substring(0, 1).toUpperCase(LOCALE_ES) + limpio.substring(1);
    }

    private static final class CategoriaAccumulator {
        private BigDecimal ventas = BigDecimal.ZERO;
        private int unidades;

        private void sumar(BigDecimal venta, int cantidad) {
            ventas = ventas.add(venta);
            unidades += cantidad;
        }
    }

    private static final class ProductoAccumulator {
        private final String producto;
        private final String categoria;
        private BigDecimal ingresos = BigDecimal.ZERO;
        private int unidades;

        private ProductoAccumulator(String producto, String categoria) {
            this.producto = producto;
            this.categoria = categoria;
        }

        private void sumar(BigDecimal ingreso, int cantidad) {
            ingresos = ingresos.add(ingreso);
            unidades += cantidad;
        }

        private BigDecimal ingresos() {
            return ingresos;
        }

        private int unidades() {
            return unidades;
        }
    }
}
