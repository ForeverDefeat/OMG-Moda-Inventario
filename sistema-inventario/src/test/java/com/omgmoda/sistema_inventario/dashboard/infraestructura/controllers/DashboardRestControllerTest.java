package com.omgmoda.sistema_inventario.dashboard.infraestructura.controllers;

import com.omgmoda.sistema_inventario.dashboard.aplicacion.dto.DashboardKpisDTO;
import com.omgmoda.sistema_inventario.dashboard.aplicacion.dto.DashboardResponseDTO;
import com.omgmoda.sistema_inventario.dashboard.aplicacion.usecases.DashboardQueryService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DashboardRestControllerTest {

    private final DashboardQueryService service = mock(DashboardQueryService.class);
    private final DashboardRestController controller = new DashboardRestController(service);

    @Test
    void obtieneDashboardConFiltros() {
        DashboardResponseDTO dto = new DashboardResponseDTO(
                new DashboardKpisDTO(BigDecimal.ZERO, 0, 0, BigDecimal.ZERO, 0),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
        when(service.obtenerDashboard("today", "all", "Camisas")).thenReturn(dto);

        var response = controller.obtenerDashboard("today", "all", "Camisas");

        assertThat(response.getBody()).isSameAs(dto);
        verify(service).obtenerDashboard("today", "all", "Camisas");
    }
}
