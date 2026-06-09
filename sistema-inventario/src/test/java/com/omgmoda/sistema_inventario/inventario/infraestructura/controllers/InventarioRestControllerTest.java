package com.omgmoda.sistema_inventario.inventario.infraestructura.controllers;

import com.omgmoda.sistema_inventario.inventario.aplicacion.dto.RegistrarEntradaDTO;
import com.omgmoda.sistema_inventario.inventario.aplicacion.ports.IRegistrarAjusteUseCase;
import com.omgmoda.sistema_inventario.inventario.aplicacion.ports.IRegistrarEntradaUseCase;
import com.omgmoda.sistema_inventario.usuario.infraestructura.security.UsuarioAutenticadoService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InventarioRestControllerTest {

    @Test
    void registrarEntradaUsaIdUsuarioAutenticado() {
        IRegistrarEntradaUseCase registrarEntradaUseCase = mock(IRegistrarEntradaUseCase.class);
        IRegistrarAjusteUseCase registrarAjusteUseCase = mock(IRegistrarAjusteUseCase.class);
        UsuarioAutenticadoService usuarioAutenticadoService = mock(UsuarioAutenticadoService.class);
        InventarioRestController controller = new InventarioRestController(
                registrarEntradaUseCase,
                registrarAjusteUseCase,
                usuarioAutenticadoService
        );
        RegistrarEntradaDTO dto = new RegistrarEntradaDTO(3L, 5, "Reposicion");
        when(usuarioAutenticadoService.obtenerIdUsuarioActual()).thenReturn(11L);

        controller.registrarEntrada(dto);

        verify(registrarEntradaUseCase).registrar(dto, 11L);
    }
}
