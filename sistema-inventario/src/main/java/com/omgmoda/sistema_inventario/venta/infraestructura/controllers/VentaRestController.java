package com.omgmoda.sistema_inventario.venta.infraestructura.controllers;

import com.omgmoda.sistema_inventario.usuario.dominio.Usuario;
import com.omgmoda.sistema_inventario.usuario.infraestructura.security.UsuarioAutenticadoService;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.CrearVentaDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.VentaResponseDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.ports.IAnularVentaUseCase;
import com.omgmoda.sistema_inventario.venta.aplicacion.ports.IConsultarVentaUseCase;
import com.omgmoda.sistema_inventario.venta.aplicacion.ports.IRegistrarVentaUseCase;
import com.omgmoda.sistema_inventario.venta.dominio.EstadoVenta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ventas")
@Tag(name = "Ventas", description = "Registro, consulta y anulacion de ventas.")
@SecurityRequirement(name = "bearer-jwt")
public class VentaRestController {

    private final IRegistrarVentaUseCase registrarVentaUseCase;
    private final IConsultarVentaUseCase consultarVentaUseCase;
    private final IAnularVentaUseCase anularVentaUseCase;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public VentaRestController(IRegistrarVentaUseCase registrarVentaUseCase,
                               IConsultarVentaUseCase consultarVentaUseCase,
                               IAnularVentaUseCase anularVentaUseCase,
                               UsuarioAutenticadoService usuarioAutenticadoService) {
        this.registrarVentaUseCase = registrarVentaUseCase;
        this.consultarVentaUseCase = consultarVentaUseCase;
        this.anularVentaUseCase = anularVentaUseCase;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @Operation(
            summary = "Registrar venta",
            description = "Crea una venta, descuenta stock y asigna el usuario autenticado desde el JWT."
    )
    public ResponseEntity<VentaResponseDTO> crearVenta(@Valid @RequestBody CrearVentaDTO dto) {
        Long idUsuario = usuarioAutenticadoService.obtenerIdUsuarioActual();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(registrarVentaUseCase.registrar(dto, idUsuario));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @Operation(summary = "Obtener venta por id", description = "Retorna el detalle de una venta.")
    public ResponseEntity<VentaResponseDTO> obtenerVenta(
            @Parameter(description = "Identificador de la venta") @PathVariable Long id) {
        return ResponseEntity.ok(consultarVentaUseCase.buscarPorId(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @Operation(
            summary = "Listar ventas",
            description = "ADMIN consulta todas las ventas; VENDEDOR consulta solo sus ventas. Permite filtros por estado o rango de fechas."
    )
    public ResponseEntity<List<VentaResponseDTO>> listarVentas(
            @Parameter(description = "Estado de venta: PENDIENTE, COMPLETADA o ANULADA")
            @RequestParam(required = false) EstadoVenta estado,
            @Parameter(description = "Fecha inicial ISO-8601. Ejemplo: 2026-06-01T00:00:00")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @Parameter(description = "Fecha final ISO-8601. Ejemplo: 2026-06-30T23:59:59")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {

        Usuario usuario = usuarioAutenticadoService.obtenerUsuarioActual();

        if (usuario.esAdmin()) {
            return listarVentasComoAdmin(estado, desde, hasta);
        }

        Long idUsuario = usuario.getId();
        if (estado != null) {
            return ResponseEntity.ok(consultarVentaUseCase.buscarPorUsuarioYEstado(idUsuario, estado));
        }
        if (desde != null && hasta != null) {
            return ResponseEntity.ok(consultarVentaUseCase.buscarPorUsuarioYFechas(idUsuario, desde, hasta));
        }

        return ResponseEntity.ok(consultarVentaUseCase.buscarPorUsuario(idUsuario));
    }

    @PatchMapping("/{id}/anular")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Anular venta",
            description = "Anula una venta completada y repone el stock. Requiere rol ADMIN."
    )
    public ResponseEntity<VentaResponseDTO> anularVenta(
            @Parameter(description = "Identificador de la venta") @PathVariable Long id) {
        Long idUsuario = usuarioAutenticadoService.obtenerIdUsuarioActual();
        return ResponseEntity.ok(anularVentaUseCase.anular(id, idUsuario));
    }

    private ResponseEntity<List<VentaResponseDTO>> listarVentasComoAdmin(
            EstadoVenta estado,
            LocalDateTime desde,
            LocalDateTime hasta) {

        if (estado != null) {
            return ResponseEntity.ok(consultarVentaUseCase.buscarPorEstado(estado));
        }
        if (desde != null && hasta != null) {
            return ResponseEntity.ok(consultarVentaUseCase.buscarPorFechas(desde, hasta));
        }

        return ResponseEntity.ok(consultarVentaUseCase.buscarTodas());
    }
}
