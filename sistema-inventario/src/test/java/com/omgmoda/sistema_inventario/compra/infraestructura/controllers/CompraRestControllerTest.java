package com.omgmoda.sistema_inventario.compra.infraestructura.controllers;

import com.omgmoda.sistema_inventario.compra.aplicacion.usecases.CompraSugerenciaService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompraRestControllerTest {

    private final CompraSugerenciaService service = mock(CompraSugerenciaService.class);
    private final CompraRestController controller = new CompraRestController(service);

    @Test
    void listaSugerenciasDesdeServicio() {
        when(service.listarSugerencias()).thenReturn(List.of());

        var response = controller.listarSugerencias();

        assertThat(response.getBody()).isEmpty();
        verify(service).listarSugerencias();
    }
}
