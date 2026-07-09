package com.omgmoda.sistema_inventario.reportes.aplicacion.usecases;

import com.omgmoda.sistema_inventario.producto.aplicacion.dto.VarianteResponseDTO;
import com.omgmoda.sistema_inventario.reportes.aplicacion.dto.MetricDatumDTO;
import com.omgmoda.sistema_inventario.reportes.aplicacion.dto.ReporteResumenDTO;
import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Servicio de aplicacion que coordina reglas de negocio y dependencias para ReportesDownloadExportService.
 */
@Service
public class ReportesDownloadExportService {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final float MARGIN = 36;
    private static final Color GREEN = new Color(0, 132, 96);
    private static final Color DARK = new Color(38, 38, 38);
    private static final Color MID = new Color(95, 105, 112);
    private static final Color LIGHT = new Color(241, 244, 242);
    private static final Color ROW = new Color(248, 249, 248);

    private final ReportesQueryService reportesQueryService;

    public ReportesDownloadExportService(ReportesQueryService reportesQueryService) {
        this.reportesQueryService = reportesQueryService;
    }

    public byte[] exportarCsv(String tipo, String periodo) {
        return exportarCsv(tipo, periodo, null, null);
    }

    public byte[] exportarCsv(String tipo, String periodo, LocalDateTime desde, LocalDateTime hasta) {
        ReportData data = dataFor(tipo, periodo, desde, hasta);
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append(String.join(",", data.headers().stream().map(this::csvCell).toList())).append('\n');
        for (List<String> row : data.rows()) {
            csv.append(String.join(",", row.stream().map(this::csvCell).toList())).append('\n');
        }
        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public byte[] exportarPdf(String tipo, String periodo) {
        return exportarPdf(tipo, periodo, null, null, "No disponible");
    }

    public byte[] exportarPdf(String tipo, String periodo, LocalDateTime desde, LocalDateTime hasta) {
        return exportarPdf(tipo, periodo, desde, hasta, "No disponible");
    }

    public byte[] exportarPdf(String tipo,
                              String periodo,
                              LocalDateTime desde,
                              LocalDateTime hasta,
                              String usuarioGenerador) {
        ReportContent report = buildReport(tipo, periodo, desde, hasta, usuarioGenerador);
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            drawPageOne(document, report);
            drawPageTwo(document, report);
            if (!report.alertas().isEmpty() || !report.bajaRotacion().isEmpty() || !report.recomendaciones().isEmpty()) {
                drawPageThree(document, report);
            }
            document.save(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el PDF del reporte.", ex);
        }
    }

    public String filename(String tipo, String periodo, String extension) {
        return filename(tipo, periodo, extension, null, null);
    }

    public String filename(String tipo, String periodo, String extension, LocalDateTime desde, LocalDateTime hasta) {
        return "reporte-" + normalizeTipo(tipo) + "-" + suffixPeriodo(periodo, desde, hasta) + "." + extension;
    }

    private ReportData dataFor(String tipo, String periodo, LocalDateTime desde, LocalDateTime hasta) {
        String normalized = normalizeTipo(tipo);
        String periodoNormalizado = normalizePeriodo(periodo);
        return switch (normalized) {
            case "resumen" -> resumenData(periodoNormalizado, desde, hasta);
            case "rotacion" -> metricData("Rotacion de inventario", rotacionData(periodoNormalizado, desde, hasta));
            default -> throw new DomainException("Tipo de reporte no soportado: " + tipo);
        };
    }

    private ReportData resumenData(String periodo, LocalDateTime desde, LocalDateTime hasta) {
        ReporteResumenDTO resumen = usaRango(desde, hasta)
                ? reportesQueryService.obtenerResumen(desde, hasta)
                : reportesQueryService.obtenerResumen(periodo);
        return new ReportData(
                "Resumen de reportes",
                List.of("Indicador", "Valor"),
                List.of(
                        List.of("Ventas del periodo", resumen.ventasMes().toPlainString()),
                        List.of("Crecimiento porcentual", resumen.crecimientoPorcentaje().toPlainString() + "%"),
                        List.of("Unidades vendidas", String.valueOf(resumen.unidadesVendidas())),
                        List.of("Ticket promedio", resumen.ticketPromedio().toPlainString()),
                        List.of("Categoria principal", resumen.categoriaPrincipal()),
                        List.of("Producto mas vendido", resumen.productoMasVendido()),
                        List.of("SKUs con alerta", String.valueOf(resumen.skusConAlerta())),
                        List.of("Reportes activos", String.valueOf(resumen.reportesActivos()))
                )
        );
    }

    private ReportContent buildReport(String tipo,
                                      String periodo,
                                      LocalDateTime desde,
                                      LocalDateTime hasta,
                                      String usuarioGenerador) {
        String normalized = normalizeTipo(tipo);
        if (!normalized.equals("resumen") && !normalized.equals("rotacion")) {
            throw new DomainException("Tipo de reporte no soportado: " + tipo);
        }
        String periodoNormalizado = normalizePeriodo(periodo);
        ReportesQueryService.Rango rango = usaRango(desde, hasta)
                ? new ReportesQueryService.Rango(desde, hasta)
                : reportesQueryService.rangoPorPeriodo(periodoNormalizado);

        ReporteResumenDTO resumen = reportesQueryService.obtenerResumen(rango.desde(), rango.hasta());
        List<MetricDatumDTO> tendencia = reportesQueryService.ventasTendencia(rango.desde(), rango.hasta());
        List<ReportesQueryService.CategoriaVentaDTO> categorias =
                reportesQueryService.ventasCategoriaDetalle(rango.desde(), rango.hasta());
        List<ReportesQueryService.ProductoVentaDTO> productos =
                reportesQueryService.productosMasVendidos(rango.desde(), rango.hasta()).stream().limit(8).toList();
        List<MetricDatumDTO> rotacion = reportesQueryService.rotacion(rango.desde(), rango.hasta(), null);
        List<VarianteResponseDTO> alertas = reportesQueryService.stockAlertas().stream().limit(14).toList();
        List<ReportesQueryService.ProductoRotacionDTO> bajaRotacion =
                reportesQueryService.productosBajaRotacion(rango.desde(), rango.hasta()).stream().limit(10).toList();

        String title = switch (normalized) {
            case "rotacion" -> "Reporte de Rotacion de Inventario";
            case "resumen" -> "Resumen Ejecutivo de Ventas";
            default -> "Reporte de Ventas e Inventario";
        };
        String rangoLabel = labelPeriodo(periodoNormalizado, rango.desde(), rango.hasta());
        String ejecutivo = resumenEjecutivo(resumen, normalized);
        List<String> recomendaciones = recomendaciones(categorias, alertas, bajaRotacion);
        return new ReportContent(
                normalized,
                title,
                rangoLabel,
                LocalDateTime.now().format(TIMESTAMP_FORMAT),
                blankToDefault(usuarioGenerador, "No disponible"),
                resumen,
                tendencia,
                categorias,
                productos,
                rotacion,
                alertas,
                bajaRotacion,
                recomendaciones,
                ejecutivo
        );
    }

    private void drawPageOne(PDDocument document, ReportContent report) throws IOException {
        PageCanvas page = newPage(document, report, 1);
        try (PDPageContentStream content = page.content()) {
            drawKpiCards(content, report);
            float chartY = 392;
            drawChartImage(document, content, lineChart(report.tendencia()), MARGIN, chartY, 250, 150);
            drawBoxTitle(content, "Rendimiento de ventas", MARGIN, chartY + 160);
            drawChartImage(document, content, donutChart(report.categorias()), 318, chartY, 220, 150);
            drawBoxTitle(content, "Ventas por categoria", 318, chartY + 160);
            drawExecutiveSummary(content, report);
        }
    }

    private void drawPageTwo(PDDocument document, ReportContent report) throws IOException {
        PageCanvas page = newPage(document, report, 2);
        try (PDPageContentStream content = page.content()) {
            drawSectionTitle(content, "Ventas por categoria", MARGIN, 718);
            drawTable(content,
                    List.of("Categoria", "Ventas", "Unidades", "Part. %"),
                    report.categorias().stream()
                            .limit(7)
                            .map(item -> List.of(
                                    item.categoria(),
                                    money(item.ventas()),
                                    String.valueOf(item.unidadesVendidas()),
                                    percent(item.participacionPorcentaje())
                            ))
                            .toList(),
                    new float[] {190, 110, 80, 80},
                    MARGIN,
                    696,
                    18);

            drawSectionTitle(content, "Productos mas vendidos", MARGIN, 512);
            drawTable(content,
                    List.of("Producto", "Categoria", "Unidades", "Ingresos"),
                    report.productos().stream()
                            .limit(7)
                            .map(item -> List.of(
                                    item.producto(),
                                    item.categoria(),
                                    String.valueOf(item.unidadesVendidas()),
                                    money(item.ingresos())
                            ))
                            .toList(),
                    new float[] {180, 130, 80, 100},
                    MARGIN,
                    490,
                    18);

            drawBoxTitle(content, "Rotacion de inventario", MARGIN, 278);
            drawChartImage(document, content, barChart(report.rotacion()), MARGIN, 108, 500, 160);
        }
    }

    private void drawPageThree(PDDocument document, ReportContent report) throws IOException {
        PageCanvas page = newPage(document, report, 3);
        try (PDPageContentStream content = page.content()) {
            drawSectionTitle(content, "SKUs con alerta", MARGIN, 718);
            drawTable(content,
                    List.of("SKU", "Producto", "Stock", "Minimo", "Estado"),
                    report.alertas().stream()
                            .limit(8)
                            .map(item -> List.of(
                                    item.sku(),
                                    item.nombreProducto(),
                                    String.valueOf(item.stockActual()),
                                    String.valueOf(item.stockMinimo()),
                                    item.stockStatus().name()
                            ))
                            .toList(),
                    new float[] {125, 190, 60, 60, 80},
                    MARGIN,
                    696,
                    18);

            drawSectionTitle(content, "Productos con baja rotacion", MARGIN, 482);
            drawTable(content,
                    List.of("SKU", "Producto", "Vend.", "Stock", "Estado"),
                    report.bajaRotacion().stream()
                            .limit(7)
                            .map(item -> List.of(
                                    item.sku(),
                                    item.producto(),
                                    String.valueOf(item.unidadesVendidas()),
                                    String.valueOf(item.stockActual()),
                                    item.estado()
                            ))
                            .toList(),
                    new float[] {125, 210, 55, 55, 80},
                    MARGIN,
                    460,
                    18);

            drawSectionTitle(content, "Recomendaciones", MARGIN, 250);
            float y = 226;
            for (String recomendacion : report.recomendaciones()) {
                drawText(content, "- " + recomendacion, MARGIN + 10, y, 10, MID, false);
                y -= 18;
            }
        }
    }

    private PageCanvas newPage(PDDocument document, ReportContent report, int pageNumber) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDPageContentStream content = new PDPageContentStream(document, page);
        drawHeader(content, report);
        drawFooter(content, pageNumber);
        return new PageCanvas(content);
    }

    private void drawHeader(PDPageContentStream content, ReportContent report) throws IOException {
        fillRect(content, 0, 790, PDRectangle.A4.getWidth(), 52, GREEN);
        drawText(content, "OMG MODA", MARGIN, 812, 18, Color.WHITE, true);
        drawText(content, report.title(), 165, 816, 14, Color.WHITE, true);
        drawText(content, "Rango: " + report.rangoLabel(), 165, 800, 8, Color.WHITE, false);
        drawText(content, "Generado: " + report.generadoEn() + " | Usuario: " + report.usuario(), 330, 800, 8, Color.WHITE, false);
    }

    private void drawFooter(PDPageContentStream content, int pageNumber) throws IOException {
        drawLine(content, MARGIN, 42, PDRectangle.A4.getWidth() - MARGIN, 42, new Color(215, 219, 217), 0.7f);
        drawText(content, "OMG MODA - Reportes empresariales", MARGIN, 26, 8, MID, false);
        drawText(content, "Pagina " + pageNumber, 510, 26, 8, MID, false);
    }

    private void drawKpiCards(PDPageContentStream content, ReportContent report) throws IOException {
        List<Kpi> kpis = List.of(
                new Kpi("Ventas del periodo", money(report.resumen().ventasMes())),
                new Kpi("Crecimiento", percent(report.resumen().crecimientoPorcentaje())),
                new Kpi("Unidades vendidas", String.valueOf(report.resumen().unidadesVendidas())),
                new Kpi("Ticket promedio", money(report.resumen().ticketPromedio())),
                new Kpi("Categoria principal", report.resumen().categoriaPrincipal()),
                new Kpi("Producto mas vendido", report.resumen().productoMasVendido()),
                new Kpi("SKUs con alerta", String.valueOf(report.resumen().skusConAlerta())),
                new Kpi("Reportes activos", String.valueOf(report.resumen().reportesActivos()))
        );
        float x = MARGIN;
        float y = 730;
        float w = 125;
        float h = 56;
        for (int i = 0; i < kpis.size(); i++) {
            if (i == 4) {
                x = MARGIN;
                y -= 68;
            }
            drawCard(content, kpis.get(i), x, y, w, h);
            x += w + 12;
        }
    }

    private void drawCard(PDPageContentStream content, Kpi kpi, float x, float y, float w, float h) throws IOException {
        fillRect(content, x, y, w, h, LIGHT);
        drawStrokeRect(content, x, y, w, h, new Color(220, 226, 222), 0.5f);
        drawText(content, kpi.label(), x + 9, y + h - 17, 7.5f, MID, true);
        drawText(content, truncate(kpi.value(), 28), x + 9, y + 18, 12, DARK, true);
    }

    private void drawExecutiveSummary(PDPageContentStream content, ReportContent report) throws IOException {
        float x = MARGIN;
        float y = 312;
        float w = 523;
        fillRect(content, x, y - 118, w, 98, new Color(248, 251, 249));
        drawStrokeRect(content, x, y - 118, w, 98, new Color(214, 224, 218), 0.7f);
        drawText(content, "Resumen ejecutivo", x + 14, y - 42, 13, GREEN, true);
        float lineY = y - 62;
        for (String line : wrap(report.resumenEjecutivo(), 108)) {
            drawText(content, line, x + 14, lineY, 9.5f, DARK, false);
            lineY -= 14;
        }
    }

    private void drawSectionTitle(PDPageContentStream content, String title, float x, float y) throws IOException {
        drawText(content, title, x, y, 13, GREEN, true);
        drawLine(content, x, y - 6, x + 180, y - 6, GREEN, 1.2f);
    }

    private void drawBoxTitle(PDPageContentStream content, String title, float x, float y) throws IOException {
        drawText(content, title, x, y, 11, GREEN, true);
    }

    private void drawTable(PDPageContentStream content,
                           List<String> headers,
                           List<List<String>> rows,
                           float[] widths,
                           float x,
                           float y,
                           float rowHeight) throws IOException {
        float totalWidth = 0;
        for (float width : widths) totalWidth += width;
        fillRect(content, x, y - rowHeight, totalWidth, rowHeight, DARK);
        float cursor = x;
        for (int i = 0; i < headers.size(); i++) {
            drawText(content, headers.get(i), cursor + 5, y - 12, 8, Color.WHITE, true);
            cursor += widths[i];
        }

        if (rows.isEmpty()) {
            fillRect(content, x, y - (rowHeight * 2), totalWidth, rowHeight, ROW);
            drawText(content, "Sin datos para el periodo seleccionado", x + 5, y - rowHeight - 12, 8, MID, false);
            return;
        }

        float rowY = y - rowHeight;
        for (int r = 0; r < rows.size(); r++) {
            rowY -= rowHeight;
            fillRect(content, x, rowY, totalWidth, rowHeight, r % 2 == 0 ? ROW : Color.WHITE);
            drawStrokeRect(content, x, rowY, totalWidth, rowHeight, new Color(226, 228, 226), 0.3f);
            cursor = x;
            for (int c = 0; c < headers.size(); c++) {
                String value = c < rows.get(r).size() ? rows.get(r).get(c) : "";
                drawText(content, truncate(value, Math.max(8, (int) (widths[c] / 5.2))), cursor + 5, rowY + 6, 7.8f, DARK, false);
                cursor += widths[c];
            }
        }
    }

    private void drawChartImage(PDDocument document,
                                PDPageContentStream content,
                                BufferedImage image,
                                float x,
                                float y,
                                float width,
                                float height) throws IOException {
        PDImageXObject pdfImage = LosslessFactory.createFromImage(document, image);
        content.drawImage(pdfImage, x, y, width, height);
    }

    private BufferedImage lineChart(List<MetricDatumDTO> data) {
        BufferedImage image = chartCanvas(520, 300);
        Graphics2D g = image.createGraphics();
        prepare(g);
        drawChartFrame(g, 520, 300);
        if (data.isEmpty()) {
            drawEmpty(g, 520, 300);
            g.dispose();
            return image;
        }
        BigDecimal max = maxMetric(data);
        int left = 52;
        int top = 28;
        int width = 420;
        int height = 205;
        int lastX = -1;
        int lastY = -1;
        g.setStroke(new BasicStroke(4f));
        g.setColor(GREEN);
        for (int i = 0; i < data.size(); i++) {
            int x = left + (data.size() == 1 ? width / 2 : Math.round((float) i * width / (data.size() - 1)));
            int y = top + height - ratio(data.get(i).value(), max, height);
            if (lastX >= 0) g.drawLine(lastX, lastY, x, y);
            g.fillOval(x - 5, y - 5, 10, 10);
            g.setFont(new Font("SansSerif", Font.PLAIN, 16));
            g.setColor(DARK);
            g.drawString(truncate(data.get(i).name(), 8), x - 20, 265);
            g.setColor(GREEN);
            lastX = x;
            lastY = y;
        }
        g.dispose();
        return image;
    }

    private BufferedImage donutChart(List<ReportesQueryService.CategoriaVentaDTO> data) {
        BufferedImage image = chartCanvas(440, 300);
        Graphics2D g = image.createGraphics();
        prepare(g);
        drawChartFrame(g, 440, 300);
        if (data.isEmpty()) {
            drawEmpty(g, 440, 300);
            g.dispose();
            return image;
        }
        List<Color> palette = List.of(GREEN, new Color(51, 166, 190), new Color(54, 54, 54), new Color(205, 196, 0), new Color(130, 156, 148));
        BigDecimal total = data.stream().map(ReportesQueryService.CategoriaVentaDTO::ventas).reduce(BigDecimal.ZERO, BigDecimal::add);
        int start = 90;
        int i = 0;
        for (ReportesQueryService.CategoriaVentaDTO item : data.stream().limit(5).toList()) {
            int arc = total.signum() == 0 ? 0 : item.ventas().multiply(BigDecimal.valueOf(360)).divide(total, 0, RoundingMode.HALF_UP).intValue();
            g.setColor(palette.get(i % palette.size()));
            g.fillArc(42, 48, 168, 168, start, -arc);
            g.fillRect(255, 58 + i * 32, 16, 16);
            g.setColor(DARK);
            g.setFont(new Font("SansSerif", Font.PLAIN, 16));
            g.drawString(truncate(item.categoria(), 16) + " " + percent(item.participacionPorcentaje()), 278, 72 + i * 32);
            start -= arc;
            i++;
        }
        g.setColor(Color.WHITE);
        g.fillOval(88, 94, 76, 76);
        g.dispose();
        return image;
    }

    private BufferedImage barChart(List<MetricDatumDTO> data) {
        BufferedImage image = chartCanvas(900, 280);
        Graphics2D g = image.createGraphics();
        prepare(g);
        drawChartFrame(g, 900, 280);
        if (data.isEmpty()) {
            drawEmpty(g, 900, 280);
            g.dispose();
            return image;
        }
        BigDecimal max = maxMetric(data);
        int left = 58;
        int baseline = 220;
        int barWidth = Math.max(28, Math.min(70, 620 / Math.max(1, data.size())));
        int gap = 18;
        g.setFont(new Font("SansSerif", Font.PLAIN, 15));
        for (int i = 0; i < data.size(); i++) {
            int x = left + i * (barWidth + gap);
            int h = ratio(data.get(i).value(), max, 160);
            g.setColor(GREEN);
            g.fillRect(x, baseline - h, barWidth, h);
            g.setColor(DARK);
            g.drawString(String.valueOf(data.get(i).value().setScale(0, RoundingMode.HALF_UP)), x, baseline - h - 8);
            g.drawString(truncate(data.get(i).name(), 8), x - 3, baseline + 22);
        }
        g.dispose();
        return image;
    }

    private BufferedImage chartCanvas(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return image;
    }

    private void drawChartFrame(Graphics2D g, int width, int height) {
        g.setColor(new Color(230, 234, 232));
        g.drawRect(1, 1, width - 3, height - 3);
        g.setColor(new Color(246, 248, 247));
        g.fillRect(2, 2, width - 4, height - 4);
        g.setColor(new Color(214, 218, 216));
        g.drawLine(42, height - 46, width - 28, height - 46);
        g.drawLine(42, 26, 42, height - 46);
    }

    private void drawEmpty(Graphics2D g, int width, int height) {
        g.setColor(MID);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.drawString("Sin datos para graficar", width / 2 - 105, height / 2);
    }

    private void prepare(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    private BigDecimal maxMetric(List<MetricDatumDTO> data) {
        return data.stream()
                .map(MetricDatumDTO::value)
                .max(BigDecimal::compareTo)
                .filter(value -> value.signum() > 0)
                .orElse(BigDecimal.ONE);
    }

    private int ratio(BigDecimal value, BigDecimal max, int height) {
        if (max.signum() == 0) return 0;
        return value.multiply(BigDecimal.valueOf(height)).divide(max, 0, RoundingMode.HALF_UP).intValue();
    }

    private void drawText(PDPageContentStream content,
                          String text,
                          float x,
                          float y,
                          float size,
                          Color color,
                          boolean bold) throws IOException {
        content.beginText();
        content.setNonStrokingColor(color);
        content.setFont(new PDType1Font(bold ? Standard14Fonts.FontName.HELVETICA_BOLD : Standard14Fonts.FontName.HELVETICA), size);
        content.newLineAtOffset(x, y);
        content.showText(safePdfText(text));
        content.endText();
        content.setNonStrokingColor(Color.BLACK);
    }

    private void fillRect(PDPageContentStream content, float x, float y, float w, float h, Color color) throws IOException {
        content.setNonStrokingColor(color);
        content.addRect(x, y, w, h);
        content.fill();
        content.setNonStrokingColor(Color.BLACK);
    }

    private void drawStrokeRect(PDPageContentStream content, float x, float y, float w, float h, Color color, float lineWidth) throws IOException {
        content.setStrokingColor(color);
        content.setLineWidth(lineWidth);
        content.addRect(x, y, w, h);
        content.stroke();
        content.setStrokingColor(Color.BLACK);
    }

    private void drawLine(PDPageContentStream content, float x1, float y1, float x2, float y2, Color color, float lineWidth) throws IOException {
        content.setStrokingColor(color);
        content.setLineWidth(lineWidth);
        content.moveTo(x1, y1);
        content.lineTo(x2, y2);
        content.stroke();
        content.setStrokingColor(Color.BLACK);
    }

    private List<String> recomendaciones(List<ReportesQueryService.CategoriaVentaDTO> categorias,
                                         List<VarianteResponseDTO> alertas,
                                         List<ReportesQueryService.ProductoRotacionDTO> bajaRotacion) {
        List<String> result = new ArrayList<>();
        if (!alertas.isEmpty()) {
            result.add("Reponer productos criticos: " + alertas.size() + " SKU(s) estan en alerta de stock.");
        }
        categorias.stream()
                .min(Comparator.comparing(ReportesQueryService.CategoriaVentaDTO::participacionPorcentaje))
                .ifPresent(item -> result.add("Revisar la categoria " + item.categoria() + " por baja participacion relativa."));
        if (!bajaRotacion.isEmpty()) {
            result.add("Promocionar productos con stock alto y baja salida para liberar inventario.");
        }
        if (result.isEmpty()) {
            result.add("Mantener el seguimiento semanal de ventas, rotacion y cobertura de stock.");
        }
        return result;
    }

    private String resumenEjecutivo(ReporteResumenDTO resumen, String tipo) {
        if (tipo.equals("rotacion")) {
            return "Durante el periodo analizado se vendieron " + resumen.unidadesVendidas()
                    + " unidades. La categoria con mayor participacion fue " + resumen.categoriaPrincipal()
                    + " y el producto con mayor salida fue " + resumen.productoMasVendido()
                    + ". Se identificaron " + resumen.skusConAlerta()
                    + " SKU(s) con alerta para priorizar reposicion y control de inventario.";
        }
        return "Durante el periodo analizado se registraron " + money(resumen.ventasMes())
                + " en ventas, con un crecimiento de " + percent(resumen.crecimientoPorcentaje())
                + ". La categoria con mayor participacion fue " + resumen.categoriaPrincipal()
                + " y el producto mas vendido fue " + resumen.productoMasVendido()
                + ". El ticket promedio fue " + money(resumen.ticketPromedio())
                + " con " + resumen.unidadesVendidas() + " unidades vendidas.";
    }

    private List<MetricDatumDTO> rotacionData(String periodo, LocalDateTime desde, LocalDateTime hasta) {
        if (usaRango(desde, hasta)) {
            return reportesQueryService.rotacion(desde, hasta, null);
        }
        return reportesQueryService.rotacionPorPeriodo(null, periodo);
    }

    private ReportData metricData(String title, List<MetricDatumDTO> metrics) {
        List<List<String>> rows = metrics.stream()
                .map(item -> List.of(item.name(), toPlainString(item.value())))
                .toList();
        return new ReportData(title, List.of("Nombre", "Valor"), rows);
    }

    private String csvCell(String value) {
        String safe = value == null ? "" : value;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }

    private String toPlainString(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal.toPlainString();
        }
        return String.valueOf(value);
    }

    private String money(BigDecimal value) {
        return "S/ " + value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String percent(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";
    }

    private String normalizeTipo(String tipo) {
        return tipo == null ? "" : tipo.toLowerCase(Locale.ROOT).trim();
    }

    private String normalizePeriodo(String periodo) {
        String normalized = periodo == null ? "7d" : periodo.toLowerCase(Locale.ROOT).trim();
        return switch (normalized) {
            case "today", "30d", "120d" -> normalized;
            default -> "7d";
        };
    }

    private String labelPeriodo(String periodo, LocalDateTime desde, LocalDateTime hasta) {
        if (usaRango(desde, hasta)) {
            return desde.toLocalDate() + " a " + hasta.toLocalDate();
        }
        return switch (normalizePeriodo(periodo)) {
            case "today" -> "Hoy";
            case "120d" -> "Ultimos 120 dias";
            case "30d" -> "Ultimos 30 dias";
            default -> "Ultimos 7 dias";
        };
    }

    private String suffixPeriodo(String periodo, LocalDateTime desde, LocalDateTime hasta) {
        if (usaRango(desde, hasta)) {
            return desde.format(DATE_FORMAT) + "-" + hasta.format(DATE_FORMAT);
        }
        return normalizePeriodo(periodo);
    }

    private boolean usaRango(LocalDateTime desde, LocalDateTime hasta) {
        return desde != null && hasta != null;
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String safePdfText(String value) {
        return blankToDefault(value, "").replace('\n', ' ').replace('\r', ' ');
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return "";
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private List<String> wrap(String value, int maxLength) {
        List<String> lines = new ArrayList<>();
        String[] words = value.split(" ");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            if (current.length() + word.length() + 1 > maxLength) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                if (!current.isEmpty()) current.append(' ');
                current.append(word);
            }
        }
        if (!current.isEmpty()) lines.add(current.toString());
        return lines.stream().limit(5).toList();
    }

    private record ReportData(String title, List<String> headers, List<List<String>> rows) {}

    private record Kpi(String label, String value) {}

    private record PageCanvas(PDPageContentStream content) {}

    private record ReportContent(String tipo,
                                 String title,
                                 String rangoLabel,
                                 String generadoEn,
                                 String usuario,
                                 ReporteResumenDTO resumen,
                                 List<MetricDatumDTO> tendencia,
                                 List<ReportesQueryService.CategoriaVentaDTO> categorias,
                                 List<ReportesQueryService.ProductoVentaDTO> productos,
                                 List<MetricDatumDTO> rotacion,
                                 List<VarianteResponseDTO> alertas,
                                 List<ReportesQueryService.ProductoRotacionDTO> bajaRotacion,
                                 List<String> recomendaciones,
                                 String resumenEjecutivo) {}
}
