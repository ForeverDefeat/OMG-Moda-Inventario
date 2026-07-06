package com.omgmoda.sistema_inventario.venta.infraestructura.controllers;

import com.omgmoda.sistema_inventario.usuario.dominio.RolUsuario;
import com.omgmoda.sistema_inventario.usuario.dominio.Usuario;
import com.omgmoda.sistema_inventario.usuario.infraestructura.security.UsuarioAutenticadoService;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.VentaResponseDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.ports.IAnularVentaUseCase;
import com.omgmoda.sistema_inventario.venta.aplicacion.ports.IConsultarVentaUseCase;
import com.omgmoda.sistema_inventario.venta.aplicacion.ports.IRegistrarVentaUseCase;
import com.omgmoda.sistema_inventario.venta.aplicacion.usecases.SecurePosService;
import com.omgmoda.sistema_inventario.venta.dominio.EstadoVenta;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class VentaRestControllerTest {

    private final IRegistrarVentaUseCase registrarVentaUseCase = mock(IRegistrarVentaUseCase.class);
    private final IConsultarVentaUseCase consultarVentaUseCase = mock(IConsultarVentaUseCase.class);
    private final IAnularVentaUseCase anularVentaUseCase = mock(IAnularVentaUseCase.class);
    private final UsuarioAutenticadoService usuarioAutenticadoService = mock(UsuarioAutenticadoService.class);
    private final SecurePosService securePosService = mock(SecurePosService.class);
    private final Environment environment = mock(Environment.class);

    @Test
    void adminListaTodasLasVentasSinFiltros() {
        VentaRestController controller = crearController();
        when(usuarioAutenticadoService.obtenerUsuarioActual()).thenReturn(usuario(1L, RolUsuario.ADMIN));
        when(consultarVentaUseCase.buscarPorEstado(EstadoVenta.COMPLETED)).thenReturn(List.of());

        controller.listarVentas(null, null, null);

        verify(consultarVentaUseCase).buscarPorEstado(EstadoVenta.COMPLETED);
    }

    @Test
    void vendedorFiltraPorEstadoSoloSobreSusVentas() {
        VentaRestController controller = crearController();
        when(usuarioAutenticadoService.obtenerUsuarioActual()).thenReturn(usuario(9L, RolUsuario.VENDEDOR));
        when(consultarVentaUseCase.buscarPorUsuarioYEstado(9L, EstadoVenta.COMPLETED))
                .thenReturn(List.<VentaResponseDTO>of());

        controller.listarVentas("COMPLETADA", null, null);

        verify(consultarVentaUseCase).buscarPorUsuarioYEstado(9L, EstadoVenta.COMPLETED);
        verifyNoInteractions(registrarVentaUseCase, anularVentaUseCase);
    }

    @Test
    void vendedorNoPuedeObtenerVentaDeOtroUsuarioPorId() {
        VentaRestController controller = crearController();
        when(usuarioAutenticadoService.obtenerUsuarioActual()).thenReturn(usuario(9L, RolUsuario.VENDEDOR));
        when(securePosService.ventaResponse(77L)).thenReturn(venta(77L, 1L));

        assertThatThrownBy(() -> controller.obtenerVenta(77L))
                .isInstanceOf(AccessDeniedException.class);
    }

    private VentaRestController crearController() {
        return new VentaRestController(
                registrarVentaUseCase,
                consultarVentaUseCase,
                anularVentaUseCase,
                usuarioAutenticadoService,
                securePosService,
                environment,
                "secret"
        );
    }

    private Usuario usuario(Long id, RolUsuario rol) {
        return new Usuario(id, "Usuario", "usuario@omgmoda.com", "hash", rol);
    }

    private VentaResponseDTO venta(Long idVenta, Long idUsuario) {
        return new VentaResponseDTO(
                idVenta,
                idUsuario,
                EstadoVenta.COMPLETED,
                "EFECTIVO",
                LocalDateTime.now(),
                List.of(),
                BigDecimal.ZERO
        );
    }
}
