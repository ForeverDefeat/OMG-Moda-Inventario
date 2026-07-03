package com.omgmoda.sistema_inventario.reportes.infraestructura.controllers;

import com.omgmoda.sistema_inventario.reportes.aplicacion.dto.ReporteResumenDTO;
import com.omgmoda.sistema_inventario.reportes.aplicacion.usecases.ReportesQueryService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReporteRestControllerTest {

    private final ReportesQueryService service = mock(ReportesQueryService.class);
    private final ReporteRestController controller = new ReporteRestController(service);

    @Test
    void resumenDelegarAlServicio() {
        when(service.obtenerResumen(null, null)).thenReturn(new ReporteResumenDTO(BigDecimal.ZERO, BigDecimal.ZERO, 0, 5));

        var response = controller.resumen(null, null);

        assertThat(response.getBody().reportesActivos()).isEqualTo(5);
        verify(service).obtenerResumen(null, null);
    }

    @Test
    void rotacionDelegarCategoriaYMeses() {
        when(service.rotacion("Camisas", 6)).thenReturn(List.of());

        controller.rotacion("Camisas", 6);

        verify(service).rotacion("Camisas", 6);
    }
}
