package com.omgmoda.sistema_inventario.venta.infraestructura.controllers;

import com.omgmoda.sistema_inventario.usuario.dominio.RolUsuario;
import com.omgmoda.sistema_inventario.usuario.dominio.Usuario;
import com.omgmoda.sistema_inventario.usuario.infraestructura.security.UsuarioAutenticadoService;
import com.omgmoda.sistema_inventario.venta.aplicacion.usecases.SecurePosService;
import com.omgmoda.sistema_inventario.venta.dominio.EstadoPago;
import com.omgmoda.sistema_inventario.venta.dominio.MetodoPago;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PagoRestControllerTest {

    private final SecurePosService securePosService = mock(SecurePosService.class);
    private final UsuarioAutenticadoService usuarioAutenticadoService = mock(UsuarioAutenticadoService.class);
    private final Environment environment = mock(Environment.class);

    @Test
    void listarPagosDelegaFiltrosYUsuarioActual() {
        PagoRestController controller = crearController();
        Usuario admin = usuario(1L, RolUsuario.ADMIN);
        LocalDateTime desde = LocalDateTime.parse("2026-07-01T00:00:00");
        LocalDateTime hasta = LocalDateTime.parse("2026-07-05T23:59:59");
        when(usuarioAutenticadoService.obtenerUsuarioActual()).thenReturn(admin);
        when(securePosService.listarPagos(EstadoPago.PENDING, MetodoPago.PLIN, desde, hasta, "OMG", admin))
                .thenReturn(List.of());

        controller.listarPagos(EstadoPago.PENDING, MetodoPago.PLIN, desde, hasta, "OMG");

        verify(securePosService).listarPagos(EstadoPago.PENDING, MetodoPago.PLIN, desde, hasta, "OMG", admin);
    }

    @Test
    void obtenerPagoUsaUsuarioActualParaValidarAcceso() {
        PagoRestController controller = crearController();
        Usuario vendedor = usuario(2L, RolUsuario.VENDEDOR);
        when(usuarioAutenticadoService.obtenerUsuarioActual()).thenReturn(vendedor);

        controller.obtenerPago(10L);

        verify(securePosService).obtenerPago(10L, vendedor);
    }

    @Test
    void detallePagoUsaUsuarioActualParaValidarAcceso() {
        PagoRestController controller = crearController();
        Usuario vendedor = usuario(2L, RolUsuario.VENDEDOR);
        when(usuarioAutenticadoService.obtenerUsuarioActual()).thenReturn(vendedor);

        controller.detallePago(10L);

        verify(securePosService).detallePago(10L, vendedor);
    }

    private PagoRestController crearController() {
        return new PagoRestController(
                securePosService,
                usuarioAutenticadoService,
                environment,
                "secret"
        );
    }

    private Usuario usuario(Long id, RolUsuario rol) {
        return new Usuario(id, "Usuario", "usuario@omgmoda.com", "hash", rol);
    }
}
