package com.omgmoda.sistema_inventario.usuario.infraestructura.controllers;

import com.omgmoda.sistema_inventario.usuario.aplicacion.dto.ActualizarRolUsuarioDTO;
import com.omgmoda.sistema_inventario.usuario.aplicacion.dto.CrearUsuarioDTO;
import com.omgmoda.sistema_inventario.usuario.aplicacion.dto.ResetearContraseniaDTO;
import com.omgmoda.sistema_inventario.usuario.aplicacion.dto.UsuarioResponseDTO;
import com.omgmoda.sistema_inventario.usuario.aplicacion.usecases.UsuarioAdminService;
import com.omgmoda.sistema_inventario.usuario.dominio.RolUsuario;
import com.omgmoda.sistema_inventario.usuario.infraestructura.security.UsuarioAutenticadoService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioRestControllerTest {

    private final UsuarioAdminService usuarioAdminService = mock(UsuarioAdminService.class);
    private final UsuarioAutenticadoService usuarioAutenticadoService = mock(UsuarioAutenticadoService.class);
    private final UsuarioRestController controller = new UsuarioRestController(usuarioAdminService, usuarioAutenticadoService);

    @Test
    void listarDelegarAlServicioAdmin() {
        when(usuarioAdminService.listar()).thenReturn(List.of());

        var response = controller.listar();

        assertThat(response.getBody()).isEmpty();
        verify(usuarioAdminService).listar();
    }

    @Test
    void crearRetornaCreated() {
        CrearUsuarioDTO dto = new CrearUsuarioDTO("Ana", "ana@omgmoda.com", "secreto1", RolUsuario.VENDEDOR);
        when(usuarioAdminService.crear(dto))
                .thenReturn(new UsuarioResponseDTO(1L, "Ana", "ana@omgmoda.com", RolUsuario.VENDEDOR, true));

        var response = controller.crear(dto);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody().activo()).isTrue();
    }

    @Test
    void cambiarRolUsaUsuarioActualParaValidarAdminActivo() {
        when(usuarioAutenticadoService.obtenerIdUsuarioActual()).thenReturn(7L);
        ActualizarRolUsuarioDTO dto = new ActualizarRolUsuarioDTO(RolUsuario.VENDEDOR);

        controller.cambiarRol(1L, dto);

        verify(usuarioAdminService).cambiarRol(1L, RolUsuario.VENDEDOR, 7L);
    }

    @Test
    void resetearContraseniaDelegarAlServicio() {
        ResetearContraseniaDTO dto = new ResetearContraseniaDTO("nueva123");

        controller.resetearContrasenia(2L, dto);

        verify(usuarioAdminService).resetearContrasenia(2L, "nueva123");
    }

    @Test
    void desactivarYReactivarDeleganAlServicio() {
        when(usuarioAutenticadoService.obtenerIdUsuarioActual()).thenReturn(7L);

        controller.desactivar(2L);
        controller.reactivar(2L);

        verify(usuarioAdminService).desactivar(2L, 7L);
        verify(usuarioAdminService).reactivar(2L);
    }
}
