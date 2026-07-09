package com.omgmoda.sistema_inventario.venta.infraestructura.controllers;

import com.omgmoda.sistema_inventario.usuario.dominio.Usuario;
import com.omgmoda.sistema_inventario.usuario.infraestructura.security.UsuarioAutenticadoService;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.ConfirmarEfectivoDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.ConfirmarManualPagoDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.PaymentDetailResponseDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.PaymentIntentResponseDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.PaymentListItemResponseDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.VentaResponseDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.WebhookStubPagoDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.usecases.SecurePosService;
import com.omgmoda.sistema_inventario.venta.dominio.EstadoPago;
import com.omgmoda.sistema_inventario.venta.dominio.MetodoPago;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador REST para exponer las operaciones de PagoRestController mediante endpoints HTTP.
 */
@RestController
@RequestMapping("/api/v1/pagos")
public class PagoRestController {

    private final SecurePosService securePosService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final Environment environment;
    private final String stubWebhookSecret;

    public PagoRestController(SecurePosService securePosService,
                              UsuarioAutenticadoService usuarioAutenticadoService,
                              Environment environment,
                              @Value("${payments.stub.webhook-secret:dev-stub-secret}") String stubWebhookSecret) {
        this.securePosService = securePosService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.environment = environment;
        this.stubWebhookSecret = stubWebhookSecret;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<List<PaymentListItemResponseDTO>> listarPagos(
            @RequestParam(required = false) EstadoPago status,
            @RequestParam(required = false) MetodoPago method,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) String search) {
        Usuario usuario = usuarioAutenticadoService.obtenerUsuarioActual();
        return ResponseEntity.ok(securePosService.listarPagos(status, method, desde, hasta, search, usuario));
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<PaymentIntentResponseDTO> obtenerPago(@PathVariable Long paymentId) {
        Usuario usuario = usuarioAutenticadoService.obtenerUsuarioActual();
        return ResponseEntity.ok(securePosService.obtenerPago(paymentId, usuario));
    }

    @GetMapping("/{paymentId}/detalle")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<PaymentDetailResponseDTO> detallePago(@PathVariable Long paymentId) {
        Usuario usuario = usuarioAutenticadoService.obtenerUsuarioActual();
        return ResponseEntity.ok(securePosService.detallePago(paymentId, usuario));
    }

    @PostMapping("/{paymentId}/confirmar-efectivo")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<VentaResponseDTO> confirmarEfectivo(
            @PathVariable Long paymentId,
            @Valid @RequestBody ConfirmarEfectivoDTO dto) {
        Usuario usuario = usuarioAutenticadoService.obtenerUsuarioActual();
        return ResponseEntity.ok(securePosService.confirmarEfectivo(paymentId, dto, usuario));
    }

    @PostMapping("/{paymentId}/confirmar-manual")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VentaResponseDTO> confirmarManual(
            @PathVariable Long paymentId,
            @Valid @RequestBody ConfirmarManualPagoDTO dto) {
        Usuario usuario = usuarioAutenticadoService.obtenerUsuarioActual();
        return ResponseEntity.ok(securePosService.confirmarManual(paymentId, dto, usuario));
    }

    @PostMapping("/{paymentId}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<VentaResponseDTO> cancelar(@PathVariable Long paymentId) {
        Usuario usuario = usuarioAutenticadoService.obtenerUsuarioActual();
        return ResponseEntity.ok(securePosService.cancelar(paymentId, usuario));
    }

    @PostMapping("/{paymentId}/expirar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VentaResponseDTO> expirar(@PathVariable Long paymentId) {
        Usuario usuario = usuarioAutenticadoService.obtenerUsuarioActual();
        return ResponseEntity.ok(securePosService.expirar(paymentId, usuario));
    }

    @PostMapping("/webhooks/stub")
    public ResponseEntity<PaymentIntentResponseDTO> webhookStub(
            @RequestHeader(value = "X-OMG-STUB-SECRET", required = false) String secret,
            @Valid @RequestBody WebhookStubPagoDTO dto) {
        if (!stubWebhookPermitido(secret)) {
            throw new AccessDeniedException("Webhook STUB no permitido.");
        }
        return ResponseEntity.ok(securePosService.registrarWebhookStub(dto, dto.toString()));
    }

    private boolean stubWebhookPermitido(String secret) {
        boolean devOrTest = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("dev") || profile.equalsIgnoreCase("test"));
        return devOrTest || (secret != null && secret.equals(stubWebhookSecret));
    }
}
