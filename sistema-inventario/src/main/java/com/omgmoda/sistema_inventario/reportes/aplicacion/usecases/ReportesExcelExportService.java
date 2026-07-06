package com.omgmoda.sistema_inventario.reportes.aplicacion.usecases;

import com.omgmoda.sistema_inventario.producto.aplicacion.dto.VarianteResponseDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ReportesExcelExportService {

    private static final String[] INVENTARIO_HEADERS = {
            "ID Variante",
            "Producto",
            "Categoria",
            "Marca",
            "Talla",
            "Color",
            "Stock actual",
            "Stock minimo",
            "Estado"
    };

    private final ReportesQueryService reportesQueryService;

    public ReportesExcelExportService(ReportesQueryService reportesQueryService) {
        this.reportesQueryService = reportesQueryService;
    }

    public byte[] exportarInventarioAlertas() {
        List<VarianteResponseDTO> alertas = reportesQueryService.stockAlertas();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Alertas de stock");
            CellStyle headerStyle = createHeaderStyle(workbook);

            Row header = sheet.createRow(0);
            for (int i = 0; i < INVENTARIO_HEADERS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(INVENTARIO_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (VarianteResponseDTO variante : alertas) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(variante.idVariante());
                row.createCell(1).setCellValue(variante.nombreProducto());
                row.createCell(2).setCellValue(variante.categoria());
                row.createCell(3).setCellValue(variante.marca());
                row.createCell(4).setCellValue(variante.talla());
                row.createCell(5).setCellValue(variante.color());
                row.createCell(6).setCellValue(variante.stockActual());
                row.createCell(7).setCellValue(variante.stockMinimo());
                row.createCell(8).setCellValue(variante.stockStatus().name());
            }

            for (int i = 0; i < INVENTARIO_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el reporte Excel de inventario.", ex);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }
}
