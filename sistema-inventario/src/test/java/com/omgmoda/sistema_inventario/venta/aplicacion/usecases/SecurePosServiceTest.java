package com.omgmoda.sistema_inventario.venta.aplicacion.usecases;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omgmoda.sistema_inventario.producto.infraestructura.adapters.VarianteJpaRepository;
import com.omgmoda.sistema_inventario.shared.dominio.exception.ConflictException;
import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;
import com.omgmoda.sistema_inventario.usuario.dominio.RolUsuario;
import com.omgmoda.sistema_inventario.usuario.dominio.Usuario;
import com.omgmoda.sistema_inventario.usuario.dominio.ports.IUsuarioRepository;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.ConfirmarManualPagoDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.CrearVentaDTO;
import com.omgmoda.sistema_inventario.venta.dominio.EstadoPago;
import com.omgmoda.sistema_inventario.venta.dominio.EstadoVenta;
import com.omgmoda.sistema_inventario.venta.dominio.MetodoPago;
import com.omgmoda.sistema_inventario.venta.infraestructura.adapters.PaymentAuditJpaRepository;
import com.omgmoda.sistema_inventario.venta.infraestructura.adapters.PaymentIntentJpaRepository;
import com.omgmoda.sistema_inventario.venta.infraestructura.adapters.PaymentWebhookEventJpaRepository;
import com.omgmoda.sistema_inventario.venta.infraestructura.adapters.StockReservaJpaRepository;
import com.omgmoda.sistema_inventario.venta.infraestructura.adapters.VentaIdempotencyJpaRepository;
import com.omgmoda.sistema_inventario.venta.infraestructura.adapters.VentaJpaRepository;
import com.omgmoda.sistema_inventario.venta.infraestructura.entities.PaymentIntentJpaEntity;
import com.omgmoda.sistema_inventario.venta.infraestructura.entities.VentaIdempotencyJpaEntity;
import com.omgmoda.sistema_inventario.venta.infraestructura.entities.VentaJpaEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecurePosServiceTest {

    private final VentaJpaRepository ventaRepository = mock(VentaJpaRepository.class);
    private final VarianteJpaRepository varianteRepository = mock(VarianteJpaRepository.class);
    private final PaymentIntentJpaRepository paymentRepository = mock(PaymentIntentJpaRepository.class);
    private final StockReservaJpaRepository reservaRepository = mock(StockReservaJpaRepository.class);
    private final PaymentAuditJpaRepository auditRepository = mock(PaymentAuditJpaRepository.class);
    private final PaymentWebhookEventJpaRepository webhookRepository = mock(PaymentWebhookEventJpaRepository.class);
    private final VentaIdempotencyJpaRepository idempotencyRepository = mock(VentaIdempotencyJpaRepository.class);
    private final IUsuarioRepository usuarioRepository = mock(IUsuarioRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SecurePosService service = new SecurePosService(
            ventaRepository,
            varianteRepository,
            paymentRepository,
            reservaRepository,
            auditRepository,
            webhookRepository,
            idempotencyRepository,
            usuarioRepository
    );

    @Test
    void mismaIdempotencyKeyConPayloadDiferenteRetornaConflict() {
        CrearVentaDTO original = venta(1);
        CrearVentaDTO diferente = venta(2);
        VentaIdempotencyJpaEntity idem = new VentaIdempotencyJpaEntity();
        idem.setIdempotencyKey("key-1");
        idem.setPayloadHash(hash(original));
        idem.setIdVenta(99L);
        idem.setIdPayment(88L);
        when(idempotencyRepository.findById("key-1")).thenReturn(Optional.of(idem));

        assertThatThrownBy(() -> service.crearVentaPendiente(diferente, 7L, "key-1"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("payload diferente");
    }

    @Test
    void vendedorNoPuedeConfirmarPagoDigitalManual() {
        Usuario vendedor = new Usuario(2L, "Vendedor", "vendedor@omgmoda.com", "hash", RolUsuario.VENDEDOR);

        assertThatThrownBy(() -> service.confirmarManual(
                10L,
                new ConfirmarManualPagoDTO(BigDecimal.TEN, "PEN", "OP123", "Validado en app"),
                vendedor
        )).isInstanceOf(DomainException.class)
                .hasMessageContaining("Solo ADMIN");
    }

    @Test
    void vendedorListaSoloSusPagosPendientesPorDefecto() {
        Usuario vendedor = new Usuario(2L, "Vendedor", "vendedor@omgmoda.com", "hash", RolUsuario.VENDEDOR);
        when(paymentRepository.findForBandeja(EstadoPago.PENDING, null, null, null, 2L)).thenReturn(List.of());

        service.listarPagos(null, null, null, null, null, vendedor);

        verify(paymentRepository).findForBandeja(EstadoPago.PENDING, null, null, null, 2L);
    }

    @Test
    void adminListaPagosSinFiltroDeUsuario() {
        Usuario admin = new Usuario(1L, "Admin", "admin@omgmoda.com", "hash", RolUsuario.ADMIN);
        when(paymentRepository.findForBandeja(EstadoPago.CANCELLED, MetodoPago.YAPE, null, null, null)).thenReturn(List.of());

        service.listarPagos(EstadoPago.CANCELLED, MetodoPago.YAPE, null, null, null, admin);

        verify(paymentRepository).findForBandeja(EstadoPago.CANCELLED, MetodoPago.YAPE, null, null, null);
    }

    @Test
    void vendedorNoPuedeConsultarDetalleDePagoAjeno() {
        Usuario vendedor = new Usuario(2L, "Vendedor", "vendedor@omgmoda.com", "hash", RolUsuario.VENDEDOR);
        PaymentIntentJpaEntity payment = payment(10L, 99L, MetodoPago.PLIN);
        VentaJpaEntity venta = ventaJpa(99L, 3L);
        when(paymentRepository.findById(10L)).thenReturn(Optional.of(payment));
        when(ventaRepository.findById(99L)).thenReturn(Optional.of(venta));

        assertThatThrownBy(() -> service.detallePago(10L, vendedor))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("otra venta");
    }

    private CrearVentaDTO venta(int cantidad) {
        return new CrearVentaDTO(
                "EFECTIVO",
                List.of(new CrearVentaDTO.ItemVentaDTO(1L, cantidad, BigDecimal.TEN))
        );
    }

    private PaymentIntentJpaEntity payment(Long idPayment, Long idVenta, MetodoPago method) {
        PaymentIntentJpaEntity payment = new PaymentIntentJpaEntity();
        payment.setId(idPayment);
        payment.setIdVenta(idVenta);
        payment.setMethod(method);
        payment.setProvider("STUB");
        payment.setProviderReference("OMG-99-TEST");
        payment.setAmountDue(BigDecimal.TEN);
        payment.setAmountReceived(BigDecimal.ZERO);
        payment.setChangeAmount(BigDecimal.ZERO);
        payment.setCurrency("PEN");
        payment.setStatus(EstadoPago.PENDING);
        payment.setPaymentReference("Referencia interna STUB");
        payment.setCreatedAt(LocalDateTime.now());
        payment.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        return payment;
    }

    private VentaJpaEntity ventaJpa(Long idVenta, Long idUsuario) {
        VentaJpaEntity venta = new VentaJpaEntity();
        venta.setId(idVenta);
        venta.setIdUsuario(idUsuario);
        venta.setEstado(EstadoVenta.PENDING_PAYMENT);
        venta.setMetodoPago("PLIN");
        venta.setFecha(LocalDateTime.now());
        return venta;
    }

    private String hash(CrearVentaDTO dto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String json = objectMapper.writeValueAsString(dto);
            return HexFormat.of().formatHex(digest.digest(json.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
