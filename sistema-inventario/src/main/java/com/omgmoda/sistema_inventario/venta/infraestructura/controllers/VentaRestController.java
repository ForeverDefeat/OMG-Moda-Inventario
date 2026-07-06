package com.omgmoda.sistema_inventario.venta.infraestructura.controllers;

import com.omgmoda.sistema_inventario.usuario.dominio.Usuario;
import com.omgmoda.sistema_inventario.usuario.infraestructura.security.UsuarioAutenticadoService;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.ConfirmarEfectivoDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.ConfirmarManualPagoDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.CrearVentaDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.PaymentIntentResponseDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.VentaResponseDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.WebhookStubPagoDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.ports.IAnularVentaUseCase;
import com.omgmoda.sistema_inventario.venta.aplicacion.ports.IConsultarVentaUseCase;
import com.omgmoda.sistema_inventario.venta.aplicacion.ports.IRegistrarVentaUseCase;
import com.omgmoda.sistema_inventario.venta.aplicacion.usecases.SecurePosService;
import com.omgmoda.sistema_inventario.venta.dominio.EstadoVenta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
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
    private final SecurePosService securePosService;
    private final Environment environment;
    private final String stubWebhookSecret;

    public VentaRestController(IRegistrarVentaUseCase registrarVentaUseCase,
                               IConsultarVentaUseCase consultarVentaUseCase,
                               IAnularVentaUseCase anularVentaUseCase,
                               UsuarioAutenticadoService usuarioAutenticadoService,
                               SecurePosService securePosService,
                               Environment environment,
                               @Value("${payments.stub.webhook-secret:dev-stub-secret}") String stubWebhookSecret) {
        this.registrarVentaUseCase = registrarVentaUseCase;
        this.consultarVentaUseCase = consultarVentaUseCase;
        this.anularVentaUseCase = anularVentaUseCase;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.securePosService = securePosService;
        this.environment = environment;
        this.stubWebhookSecret = stubWebhookSecret;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @Operation(
            summary = "Registrar venta",
            description = "Crea una venta, descuenta stock y asigna el usuario autenticado desde el JWT."
    )
    public ResponseEntity<VentaResponseDTO> crearVenta(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CrearVentaDTO dto) {
        Long idUsuario = usuarioAutenticadoService.obtenerIdUsuarioActual();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(securePosService.crearVentaPendiente(dto, idUsuario, idempotencyKey));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @Operation(summary = "Obtener venta por id", description = "Retorna el detalle de una venta.")
    public ResponseEntity<VentaResponseDTO> obtenerVenta(
            @Parameter(description = "Identificador de la venta") @PathVariable Long id) {
        Usuario usuario = usuarioAutenticadoService.obtenerUsuarioActual();
        VentaResponseDTO venta = securePosService.ventaResponse(id);
        if (!usuario.esAdmin() && !venta.idUsuario().equals(usuario.getId())) {
            throw new AccessDeniedException("La venta solicitada no pertenece al usuario autenticado.");
        }
        return ResponseEntity.ok(venta);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @Operation(
            summary = "Listar ventas",
            description = "ADMIN consulta todas las ventas; VENDEDOR consulta solo sus ventas. Permite filtros por estado o rango de fechas."
    )
    public ResponseEntity<List<VentaResponseDTO>> listarVentas(
            @Parameter(description = "Estado de venta")
            @RequestParam(required = false) String estado,
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
        EstadoVenta estadoNormalizado = estado == null ? EstadoVenta.COMPLETED : parseEstado(estado);
        if (estado != null) {
            return ResponseEntity.ok(consultarVentaUseCase.buscarPorUsuarioYEstado(idUsuario, estadoNormalizado));
        }
        if (desde != null && hasta != null) {
            return ResponseEntity.ok(consultarVentaUseCase.buscarPorUsuarioYFechas(idUsuario, desde, hasta));
        }

        return ResponseEntity.ok(consultarVentaUseCase.buscarPorUsuarioYEstado(idUsuario, EstadoVenta.COMPLETED));
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

    @PostMapping("/pagos/{paymentId}/confirmar-efectivo")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<VentaResponseDTO> confirmarEfectivo(
            @PathVariable Long paymentId,
            @Valid @RequestBody ConfirmarEfectivoDTO dto) {
        Usuario usuario = usuarioAutenticadoService.obtenerUsuarioActual();
        return ResponseEntity.ok(securePosService.confirmarEfectivo(paymentId, dto, usuario));
    }

    @PostMapping("/pagos/{paymentId}/confirmar-manual")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VentaResponseDTO> confirmarManual(
            @PathVariable Long paymentId,
            @Valid @RequestBody ConfirmarManualPagoDTO dto) {
        Usuario usuario = usuarioAutenticadoService.obtenerUsuarioActual();
        return ResponseEntity.ok(securePosService.confirmarManual(paymentId, dto, usuario));
    }

    @GetMapping("/pagos/{paymentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<PaymentIntentResponseDTO> obtenerPago(@PathVariable Long paymentId) {
        Usuario usuario = usuarioAutenticadoService.obtenerUsuarioActual();
        return ResponseEntity.ok(securePosService.obtenerPago(paymentId, usuario));
    }

    @PostMapping("/pagos/{paymentId}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<VentaResponseDTO> cancelarPago(@PathVariable Long paymentId) {
        Usuario usuario = usuarioAutenticadoService.obtenerUsuarioActual();
        return ResponseEntity.ok(securePosService.cancelar(paymentId, usuario));
    }

    @PostMapping("/pagos/{paymentId}/expirar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VentaResponseDTO> expirarPago(@PathVariable Long paymentId) {
        Usuario usuario = usuarioAutenticadoService.obtenerUsuarioActual();
        return ResponseEntity.ok(securePosService.expirar(paymentId, usuario));
    }

    @PostMapping("/pagos/webhooks/stub")
    public ResponseEntity<PaymentIntentResponseDTO> webhookStub(
            @RequestHeader(value = "X-OMG-STUB-SECRET", required = false) String secret,
            @Valid @RequestBody WebhookStubPagoDTO dto) {
        if (!stubWebhookPermitido(secret)) {
            throw new AccessDeniedException("Webhook STUB no permitido.");
        }
        return ResponseEntity.ok(securePosService.registrarWebhookStub(dto, dto.toString()));
    }

    private ResponseEntity<List<VentaResponseDTO>> listarVentasComoAdmin(
            String estado,
            LocalDateTime desde,
            LocalDateTime hasta) {

        EstadoVenta estadoNormalizado = estado == null ? EstadoVenta.COMPLETED : parseEstado(estado);
        if (estado != null) {
            return ResponseEntity.ok(consultarVentaUseCase.buscarPorEstado(estadoNormalizado));
        }
        if (desde != null && hasta != null) {
            return ResponseEntity.ok(consultarVentaUseCase.buscarPorFechas(desde, hasta));
        }

        return ResponseEntity.ok(consultarVentaUseCase.buscarPorEstado(EstadoVenta.COMPLETED));
    }

    private EstadoVenta parseEstado(String estado) {
        return switch (estado.trim().toUpperCase()) {
            case "PENDIENTE" -> EstadoVenta.PENDING_PAYMENT;
            case "COMPLETADA" -> EstadoVenta.COMPLETED;
            case "ANULADA" -> EstadoVenta.CANCELLED;
            default -> EstadoVenta.valueOf(estado.trim().toUpperCase()).normalizado();
        };
    }

    private boolean stubWebhookPermitido(String secret) {
        boolean devOrTest = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("dev") || profile.equalsIgnoreCase("test"));
        return devOrTest || (secret != null && secret.equals(stubWebhookSecret));
    }
}
