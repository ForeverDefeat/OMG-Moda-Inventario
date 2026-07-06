package com.omgmoda.sistema_inventario.reportes.aplicacion.usecases;

import com.omgmoda.sistema_inventario.producto.aplicacion.dto.VarianteResponseDTO;
import com.omgmoda.sistema_inventario.shared.dominio.valueobjects.StockStatus;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportesExcelExportServiceTest {

    private final ReportesQueryService reportesQueryService = mock(ReportesQueryService.class);
    private final ReportesExcelExportService service = new ReportesExcelExportService(reportesQueryService);

    @Test
    void exportaAlertasDeInventarioComoWorkbookXlsx() throws Exception {
        when(reportesQueryService.stockAlertas()).thenReturn(List.of(variante()));

        byte[] bytes = service.exportarInventarioAlertas();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheet("Alertas de stock");
            assertThat(sheet).isNotNull();
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("ID Variante");
            assertThat(sheet.getRow(0).getCell(1).getStringCellValue()).isEqualTo("Producto");
            assertThat(sheet.getRow(1).getCell(1).getStringCellValue()).isEqualTo("Camisa Oxford");
            assertThat(sheet.getRow(1).getCell(8).getStringCellValue()).isEqualTo("BAJO_STOCK");
        }
    }

    private VarianteResponseDTO variante() {
        return new VarianteResponseDTO(
                10L,
                1L,
                "Camisa Oxford",
                "Camisas",
                "OMG MODA",
                null,
                "M",
                "Azul",
                "Algodon",
                BigDecimal.valueOf(45),
                BigDecimal.valueOf(89.90),
                2,
                5,
                StockStatus.BAJO_STOCK
        );
    }
}
