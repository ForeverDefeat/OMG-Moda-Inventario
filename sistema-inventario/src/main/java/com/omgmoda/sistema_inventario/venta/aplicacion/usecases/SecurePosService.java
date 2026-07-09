package com.omgmoda.sistema_inventario.venta.aplicacion.usecases;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omgmoda.sistema_inventario.producto.infraestructura.adapters.VarianteJpaRepository;
import com.omgmoda.sistema_inventario.producto.infraestructura.entities.VarianteJpaEntity;
import com.omgmoda.sistema_inventario.shared.dominio.exception.ConflictException;
import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;
import com.omgmoda.sistema_inventario.shared.dominio.exception.NotFoundException;
import com.omgmoda.sistema_inventario.usuario.dominio.Usuario;
import com.omgmoda.sistema_inventario.usuario.dominio.ports.IUsuarioRepository;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.ConfirmarEfectivoDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.ConfirmarManualPagoDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.CrearVentaDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.PaymentAuditResponseDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.PaymentDetailResponseDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.PaymentIntentResponseDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.PaymentListItemResponseDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.VentaResponseDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.WebhookStubPagoDTO;
import com.omgmoda.sistema_inventario.venta.dominio.EstadoPago;
import com.omgmoda.sistema_inventario.venta.dominio.EstadoReservaStock;
import com.omgmoda.sistema_inventario.venta.dominio.EstadoVenta;
import com.omgmoda.sistema_inventario.venta.dominio.MetodoPago;
import com.omgmoda.sistema_inventario.venta.infraestructura.adapters.PaymentAuditJpaRepository;
import com.omgmoda.sistema_inventario.venta.infraestructura.adapters.PaymentIntentJpaRepository;
import com.omgmoda.sistema_inventario.venta.infraestructura.adapters.PaymentWebhookEventJpaRepository;
import com.omgmoda.sistema_inventario.venta.infraestructura.adapters.StockReservaJpaRepository;
import com.omgmoda.sistema_inventario.venta.infraestructura.adapters.VentaIdempotencyJpaRepository;
import com.omgmoda.sistema_inventario.venta.infraestructura.adapters.VentaJpaRepository;
import com.omgmoda.sistema_inventario.venta.infraestructura.entities.DetalleVentaJpaEntity;
import com.omgmoda.sistema_inventario.venta.infraestructura.entities.PaymentAuditJpaEntity;
import com.omgmoda.sistema_inventario.venta.infraestructura.entities.PaymentIntentJpaEntity;
import com.omgmoda.sistema_inventario.venta.infraestructura.entities.PaymentWebhookEventJpaEntity;
import com.omgmoda.sistema_inventario.venta.infraestructura.entities.StockReservaJpaEntity;
import com.omgmoda.sistema_inventario.venta.infraestructura.entities.VentaIdempotencyJpaEntity;
import com.omgmoda.sistema_inventario.venta.infraestructura.entities.VentaJpaEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio POS seguro: crea ventas pendientes, reserva stock, confirma pagos y deja trazabilidad auditable.
 */
@Service
public class SecurePosService {

    private static final String PEN = "PEN";
    private static final int EXPIRATION_MINUTES = 15;

    private final VentaJpaRepository ventaRepository;
    private final VarianteJpaRepository varianteRepository;
    private final PaymentIntentJpaRepository paymentRepository;
    private final StockReservaJpaRepository reservaRepository;
    private final PaymentAuditJpaRepository auditRepository;
    private final PaymentWebhookEventJpaRepository webhookRepository;
    private final VentaIdempotencyJpaRepository idempotencyRepository;
    private final IUsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SecurePosService(VentaJpaRepository ventaRepository,
                            VarianteJpaRepository varianteRepository,
                            PaymentIntentJpaRepository paymentRepository,
                            StockReservaJpaRepository reservaRepository,
                            PaymentAuditJpaRepository auditRepository,
                            PaymentWebhookEventJpaRepository webhookRepository,
                            VentaIdempotencyJpaRepository idempotencyRepository,
                            IUsuarioRepository usuarioRepository) {
        this.ventaRepository = ventaRepository;
        this.varianteRepository = varianteRepository;
        this.paymentRepository = paymentRepository;
        this.reservaRepository = reservaRepository;
        this.auditRepository = auditRepository;
        this.webhookRepository = webhookRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public VentaResponseDTO crearVentaPendiente(CrearVentaDTO dto, Long idUsuario, String idempotencyKey) {
        String normalizedKey = normalizarKey(idempotencyKey);
        String payloadHash = hashPayload(dto);
        // La idempotencia evita duplicar ventas si el POS reintenta el mismo request por timeout o error de red.
        if (normalizedKey != null) {
            var existing = idempotencyRepository.findById(normalizedKey);
            if (existing.isPresent()) {
                VentaIdempotencyJpaEntity idem = existing.get();
                if (!idem.getPayloadHash().equals(payloadHash)) {
                    throw new ConflictException("La Idempotency-Key ya fue usada con un payload diferente.");
                }
                return ventaResponse(idem.getIdVenta());
            }
        }

        MetodoPago metodo = metodo(dto.metodoPago());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(EXPIRATION_MINUTES);
        VentaJpaEntity venta = new VentaJpaEntity();
        venta.setIdUsuario(idUsuario);
        venta.setEstado(EstadoVenta.PENDING_PAYMENT);
        venta.setMetodoPago(metodo.name());
        venta.setFecha(now);
        venta.setIdempotencyKey(normalizedKey);
        venta.setIdempotencyPayloadHash(payloadHash);

        BigDecimal total = BigDecimal.ZERO;
        for (CrearVentaDTO.ItemVentaDTO item : dto.items()) {
            // El bloqueo pesimista mantiene consistente el stock reservado cuando dos cajas venden la misma variante.
            VarianteJpaEntity variante = varianteRepository.findByIdForUpdate(item.idVariante())
                    .orElseThrow(() -> new NotFoundException("Variante no encontrada con id: " + item.idVariante()));
            int disponible = variante.getStockActual() - variante.getStockReservado();
            if (disponible < item.cantidad()) {
                throw new DomainException("Stock insuficiente para variante id " + item.idVariante()
                        + ". Disponible: " + disponible + ", solicitado: " + item.cantidad());
            }
            variante.setStockReservado(variante.getStockReservado() + item.cantidad());
            DetalleVentaJpaEntity detalle = new DetalleVentaJpaEntity();
            detalle.setVenta(venta);
            detalle.setIdVariante(item.idVariante());
            detalle.setCantidad(item.cantidad());
            detalle.setPrecioUnitario(item.precioUnitario());
            venta.getDetalles().add(detalle);
            total = total.add(item.precioUnitario().multiply(BigDecimal.valueOf(item.cantidad())));
        }

        VentaJpaEntity saved = ventaRepository.save(venta);
        // La reserva separada permite expirar o cancelar la venta sin perder la trazabilidad del intento de pago.
        for (CrearVentaDTO.ItemVentaDTO item : dto.items()) {
            StockReservaJpaEntity reserva = new StockReservaJpaEntity();
            reserva.setIdVenta(saved.getId());
            reserva.setIdVariante(item.idVariante());
            reserva.setCantidad(item.cantidad());
            reserva.setEstado(EstadoReservaStock.ACTIVE);
            reserva.setCreatedAt(now);
            reserva.setExpiresAt(expiresAt);
            reservaRepository.save(reserva);
        }

        PaymentIntentJpaEntity payment = new PaymentIntentJpaEntity();
        payment.setIdVenta(saved.getId());
        payment.setMethod(metodo);
        payment.setProvider(metodo == MetodoPago.EFECTIVO ? "CASH" : "STUB");
        payment.setProviderReference("OMG-" + saved.getId() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
        payment.setAmountDue(money(total));
        payment.setAmountReceived(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        payment.setChangeAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        payment.setCurrency(PEN);
        payment.setStatus(EstadoPago.PENDING);
        payment.setPaymentReference(metodo == MetodoPago.EFECTIVO ? "Pago en caja" : "Referencia interna STUB, no es un pago real");
        payment.setCreatedAt(now);
        payment.setExpiresAt(expiresAt);
        payment = paymentRepository.save(payment);
        audit(payment, "PAYMENT_CREATED", null, EstadoPago.PENDING.name(), idUsuario, null, null, null, null, null);

        if (normalizedKey != null) {
            VentaIdempotencyJpaEntity idem = new VentaIdempotencyJpaEntity();
            idem.setIdempotencyKey(normalizedKey);
            idem.setPayloadHash(payloadHash);
            idem.setIdVenta(saved.getId());
            idem.setIdPayment(payment.getId());
            idem.setCreatedAt(now);
            idempotencyRepository.save(idem);
        }

        return ventaResponse(saved.getId());
    }

    @Transactional
    public VentaResponseDTO confirmarEfectivo(Long paymentId, ConfirmarEfectivoDTO dto, Usuario usuario) {
        PaymentIntentJpaEntity payment = paymentForUpdate(paymentId);
        if (payment.getMethod().normalizado() != MetodoPago.EFECTIVO) {
            throw new DomainException("Este pago no es de efectivo.");
        }
        BigDecimal received = money(dto.amountReceived());
        if (received.compareTo(payment.getAmountDue()) < 0) {
            throw new DomainException("El monto recibido no cubre el total de la venta.");
        }
        return confirmar(payment, EstadoPago.MANUALLY_CONFIRMED, received, dto.reference(), dto.observation(), usuario);
    }

    @Transactional
    public VentaResponseDTO confirmarManual(Long paymentId, ConfirmarManualPagoDTO dto, Usuario usuario) {
        if (!usuario.esAdmin()) {
            throw new DomainException("Solo ADMIN puede confirmar pagos digitales manualmente.");
        }
        PaymentIntentJpaEntity payment = paymentForUpdate(paymentId);
        if (!payment.getMethod().normalizado().esDigital()) {
            throw new DomainException("La confirmacion manual digital solo aplica a YAPE, PLIN o CARD.");
        }
        if (!PEN.equals(dto.currency())) {
            throw new DomainException("La moneda debe ser PEN.");
        }
        BigDecimal received = money(dto.amountReceived());
        if (received.compareTo(payment.getAmountDue()) != 0) {
            audit(payment, "PAYMENT_AMOUNT_MISMATCH", payment.getStatus().name(), payment.getStatus().name(),
                    usuario.getId(), usuario.getRol().name(), dto.reference(), dto.observation(), received, null);
            throw new DomainException("El monto pagado no coincide con el total.");
        }
        return confirmar(payment, EstadoPago.MANUALLY_CONFIRMED, received, dto.reference(), dto.observation(), usuario);
    }

    @Transactional
    public VentaResponseDTO cancelar(Long paymentId, Usuario usuario) {
        PaymentIntentJpaEntity payment = paymentForUpdate(paymentId);
        VentaJpaEntity venta = ventaForUpdate(payment.getIdVenta());
        validarAccesoVenta(venta, usuario);
        validarPendiente(venta, payment);
        liberarReservas(venta.getId(), EstadoReservaStock.RELEASED);
        EstadoPago previo = payment.getStatus();
        payment.setStatus(EstadoPago.CANCELLED);
        venta.setEstado(EstadoVenta.CANCELLED);
        venta.setCancelledAt(LocalDateTime.now());
        audit(payment, "PAYMENT_CANCELLED", previo.name(), EstadoPago.CANCELLED.name(),
                usuario.getId(), usuario.getRol().name(), null, "Venta pendiente cancelada", null, null);
        return ventaResponse(venta.getId());
    }

    @Transactional
    public VentaResponseDTO expirar(Long paymentId, Usuario usuario) {
        PaymentIntentJpaEntity payment = paymentForUpdate(paymentId);
        VentaJpaEntity venta = ventaForUpdate(payment.getIdVenta());
        validarAccesoVenta(venta, usuario);
        validarPendiente(venta, payment);
        liberarReservas(venta.getId(), EstadoReservaStock.EXPIRED);
        EstadoPago previo = payment.getStatus();
        payment.setStatus(EstadoPago.EXPIRED);
        venta.setEstado(EstadoVenta.EXPIRED);
        venta.setExpiredAt(LocalDateTime.now());
        audit(payment, "PAYMENT_EXPIRED", previo.name(), EstadoPago.EXPIRED.name(),
                usuario.getId(), usuario.getRol().name(), null, "Pago expirado. Reserva liberada.", null, null);
        return ventaResponse(venta.getId());
    }

    @Transactional
    public PaymentIntentResponseDTO registrarWebhookStub(WebhookStubPagoDTO dto, String rawPayload) {
        String payloadHash = sha256(rawPayload == null ? dto.toString() : rawPayload);
        var existing = webhookRepository.findByProviderEventId(dto.providerEventId());
        if (existing.isPresent()) {
            // Los proveedores pueden reenviar webhooks; se marca como duplicado y no se repite la accion.
            PaymentWebhookEventJpaEntity event = existing.get();
            event.setStatus("DUPLICATE");
            return paymentRepository.findByProviderReference(dto.providerReference()).map(this::paymentResponse).orElse(null);
        }
        PaymentWebhookEventJpaEntity event = new PaymentWebhookEventJpaEntity();
        event.setProvider("STUB");
        event.setProviderEventId(dto.providerEventId());
        event.setPayloadHash(payloadHash);
        event.setPayload(rawPayload);
        event.setReceivedAt(LocalDateTime.now());
        event.setStatus("RECEIVED");
        webhookRepository.save(event);

        PaymentIntentJpaEntity payment = paymentRepository.findByProviderReference(dto.providerReference())
                .orElseThrow(() -> new NotFoundException("Pago no encontrado con referencia: " + dto.providerReference()));
        audit(payment, "WEBHOOK_RECEIVED", payment.getStatus().name(), payment.getStatus().name(),
                null, null, dto.providerReference(), "Evento STUB recibido", dto.amount(), dto.providerEventId());
        event.setStatus("PROCESSED");
        event.setProcessedAt(LocalDateTime.now());
        return paymentResponse(payment);
    }

    @Transactional(readOnly = true)
    public VentaResponseDTO ventaResponse(Long idVenta) {
        VentaJpaEntity venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new NotFoundException("Venta no encontrada con id: " + idVenta));
        PaymentIntentJpaEntity payment = paymentRepository.findByIdVenta(idVenta).orElse(null);
        return ventaResponse(venta, payment);
    }

    @Transactional(readOnly = true)
    public PaymentIntentResponseDTO obtenerPago(Long paymentId) {
        return paymentResponse(paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Pago no encontrado con id: " + paymentId)));
    }

    @Transactional(readOnly = true)
    public PaymentIntentResponseDTO obtenerPago(Long paymentId, Usuario usuario) {
        PaymentIntentJpaEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Pago no encontrado con id: " + paymentId));
        VentaJpaEntity venta = ventaRepository.findById(payment.getIdVenta())
                .orElseThrow(() -> new NotFoundException("Venta no encontrada con id: " + payment.getIdVenta()));
        validarAccesoVenta(venta, usuario);
        return paymentResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentListItemResponseDTO> listarPagos(EstadoPago status,
                                                        MetodoPago method,
                                                        LocalDateTime desde,
                                                        LocalDateTime hasta,
                                                        String search,
                                                        Usuario usuario) {
        EstadoPago statusFiltro = status == null ? EstadoPago.PENDING : status;
        MetodoPago methodFiltro = method == null ? null : method.normalizado();
        Long idUsuario = usuario.esAdmin() ? null : usuario.getId();
        String searchNormalizado = normalizarBusqueda(search);
        return paymentRepository.findForBandeja(statusFiltro, methodFiltro, desde, hasta, idUsuario).stream()
                .map(this::paymentListItem)
                .filter(item -> coincideBusqueda(item, searchNormalizado))
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentDetailResponseDTO detallePago(Long paymentId, Usuario usuario) {
        PaymentIntentJpaEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Pago no encontrado con id: " + paymentId));
        VentaJpaEntity venta = ventaRepository.findById(payment.getIdVenta())
                .orElseThrow(() -> new NotFoundException("Venta no encontrada con id: " + payment.getIdVenta()));
        validarAccesoVenta(venta, usuario);
        Usuario vendedor = buscarUsuario(venta.getIdUsuario()).orElse(null);
        List<PaymentDetailResponseDTO.ProductoPagoResponseDTO> productos = venta.getDetalles().stream()
                .map(detalle -> {
                    VarianteJpaEntity variante = varianteRepository.findById(detalle.getIdVariante()).orElse(null);
                    String producto = variante == null || variante.getProducto() == null ? "Variante " + detalle.getIdVariante() : variante.getProducto().getNombre();
                    String categoria = variante == null || variante.getProducto() == null ? "Sin categoria" : variante.getProducto().getCategoria();
                    return new PaymentDetailResponseDTO.ProductoPagoResponseDTO(
                            detalle.getId(),
                            detalle.getIdVariante(),
                            variante == null ? "SKU-" + detalle.getIdVariante() : variante.getSku(),
                            producto,
                            categoria,
                            variante == null ? "Sin color" : variante.getColor(),
                            variante == null ? "Sin talla" : variante.getTalla(),
                            detalle.getCantidad(),
                            detalle.getPrecioUnitario(),
                            detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(detalle.getCantidad()))
                    );
                })
                .toList();
        List<PaymentDetailResponseDTO.ReservaPagoResponseDTO> reservas = reservaRepository.findByIdVenta(venta.getId()).stream()
                .map(reserva -> new PaymentDetailResponseDTO.ReservaPagoResponseDTO(
                        reserva.getId(),
                        reserva.getIdVariante(),
                        reserva.getCantidad(),
                        reserva.getEstado(),
                        reserva.getCreatedAt(),
                        reserva.getExpiresAt(),
                        reserva.getReleasedAt()
                ))
                .toList();
        return new PaymentDetailResponseDTO(
                paymentResponse(payment),
                ventaResponse(venta, payment),
                vendedor == null ? "Usuario " + venta.getIdUsuario() : vendedor.getNombre(),
                vendedor == null ? "No disponible" : vendedor.getCorreo(),
                productos,
                reservas
        );
    }

    private VentaResponseDTO confirmar(PaymentIntentJpaEntity payment,
                                       EstadoPago nextStatus,
                                       BigDecimal received,
                                       String reference,
                                       String observation,
                                       Usuario usuario) {
        VentaJpaEntity venta = ventaForUpdate(payment.getIdVenta());
        validarAccesoVenta(venta, usuario);
        validarPendiente(venta, payment);
        if (LocalDateTime.now().isAfter(payment.getExpiresAt())) {
            throw new DomainException("Pago expirado. Genera un nuevo cobro.");
        }
        List<StockReservaJpaEntity> reservas = reservaRepository.findByIdVentaAndEstadoForUpdate(venta.getId(), EstadoReservaStock.ACTIVE);
        if (reservas.isEmpty()) {
            throw new DomainException("La venta no tiene reserva activa de stock.");
        }
        // Confirmar consume la reserva: baja stock real y libera el saldo reservado en una misma transaccion.
        for (StockReservaJpaEntity reserva : reservas) {
            VarianteJpaEntity variante = varianteRepository.findByIdForUpdate(reserva.getIdVariante())
                    .orElseThrow(() -> new NotFoundException("Variante no encontrada con id: " + reserva.getIdVariante()));
            if (variante.getStockReservado() < reserva.getCantidad() || variante.getStockActual() < reserva.getCantidad()) {
                throw new DomainException("Stock reservado inconsistente para completar la venta.");
            }
            variante.setStockActual(variante.getStockActual() - reserva.getCantidad());
            variante.setStockReservado(variante.getStockReservado() - reserva.getCantidad());
            reserva.setEstado(EstadoReservaStock.CONSUMED);
            reserva.setReleasedAt(LocalDateTime.now());
        }
        EstadoPago previo = payment.getStatus();
        payment.setStatus(nextStatus);
        payment.setAmountReceived(received);
        payment.setChangeAmount(received.subtract(payment.getAmountDue()).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
        payment.setConfirmedAt(LocalDateTime.now());
        venta.setEstado(EstadoVenta.COMPLETED);
        venta.setCompletedAt(LocalDateTime.now());
        audit(payment, "PAYMENT_CONFIRMED", previo.name(), nextStatus.name(),
                usuario.getId(), usuario.getRol().name(), reference, observation, received, null);
        audit(payment, "SALE_COMPLETED", EstadoVenta.PENDING_PAYMENT.name(), EstadoVenta.COMPLETED.name(),
                usuario.getId(), usuario.getRol().name(), reference, observation, received, null);
        return ventaResponse(venta.getId());
    }

    private void validarPendiente(VentaJpaEntity venta, PaymentIntentJpaEntity payment) {
        if (venta.getEstado().normalizado() != EstadoVenta.PENDING_PAYMENT) {
            throw new DomainException("La venta no esta pendiente de pago.");
        }
        if (payment.getStatus() != EstadoPago.PENDING) {
            throw new DomainException("El pago no esta pendiente.");
        }
        if (!PEN.equals(payment.getCurrency())) {
            throw new DomainException("La moneda debe ser PEN.");
        }
    }

    private void liberarReservas(Long idVenta, EstadoReservaStock estadoFinal) {
        List<StockReservaJpaEntity> reservas = reservaRepository.findByIdVentaAndEstadoForUpdate(idVenta, EstadoReservaStock.ACTIVE);
        // Cancelaciones y expiraciones no consumen stock; solo devuelven la reserva al disponible.
        for (StockReservaJpaEntity reserva : reservas) {
            VarianteJpaEntity variante = varianteRepository.findByIdForUpdate(reserva.getIdVariante())
                    .orElseThrow(() -> new NotFoundException("Variante no encontrada con id: " + reserva.getIdVariante()));
            if (variante.getStockReservado() < reserva.getCantidad()) {
                throw new DomainException("Stock reservado inconsistente para liberar la reserva.");
            }
            variante.setStockReservado(variante.getStockReservado() - reserva.getCantidad());
            reserva.setEstado(estadoFinal);
            reserva.setReleasedAt(LocalDateTime.now());
        }
    }

    private void validarAccesoVenta(VentaJpaEntity venta, Usuario usuario) {
        if (usuario == null) {
            throw new DomainException("Usuario autenticado requerido.");
        }
        if (!usuario.esAdmin() && !venta.getIdUsuario().equals(usuario.getId())) {
            throw new DomainException("No puedes operar pagos de otra venta.");
        }
    }

    private PaymentListItemResponseDTO paymentListItem(PaymentIntentJpaEntity payment) {
        VentaJpaEntity venta = ventaRepository.findById(payment.getIdVenta())
                .orElseThrow(() -> new NotFoundException("Venta no encontrada con id: " + payment.getIdVenta()));
        Usuario vendedor = buscarUsuario(venta.getIdUsuario()).orElse(null);
        return new PaymentListItemResponseDTO(
                payment.getId(),
                payment.getIdVenta(),
                venta.getIdUsuario(),
                vendedor == null ? "Usuario " + venta.getIdUsuario() : vendedor.getNombre(),
                vendedor == null ? "No disponible" : vendedor.getCorreo(),
                payment.getMethod(),
                payment.getProviderReference(),
                payment.getAmountDue(),
                payment.getAmountReceived(),
                payment.getCurrency(),
                payment.getStatus(),
                venta.getEstado().normalizado(),
                payment.getCreatedAt(),
                payment.getExpiresAt(),
                payment.getConfirmedAt()
        );
    }

    private Optional<Usuario> buscarUsuario(Long idUsuario) {
        return idUsuario == null ? Optional.empty() : usuarioRepository.findById(idUsuario);
    }

    private String normalizarBusqueda(String search) {
        if (search == null || search.isBlank()) return null;
        return search.trim().toUpperCase(Locale.ROOT);
    }

    private boolean coincideBusqueda(PaymentListItemResponseDTO item, String search) {
        if (search == null) return true;
        return ("#" + item.idVenta()).contains(search)
                || String.valueOf(item.idVenta()).contains(search)
                || String.valueOf(item.idPayment()).contains(search)
                || item.providerReference().toUpperCase(Locale.ROOT).contains(search)
                || item.vendedorNombre().toUpperCase(Locale.ROOT).contains(search)
                || item.vendedorCorreo().toUpperCase(Locale.ROOT).contains(search);
    }

    private PaymentIntentJpaEntity paymentForUpdate(Long paymentId) {
        return paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new NotFoundException("Pago no encontrado con id: " + paymentId));
    }

    private VentaJpaEntity ventaForUpdate(Long idVenta) {
        return ventaRepository.findByIdForUpdate(idVenta)
                .orElseThrow(() -> new NotFoundException("Venta no encontrada con id: " + idVenta));
    }

    private MetodoPago metodo(String value) {
        MetodoPago metodo = MetodoPago.valueOf(value.trim().toUpperCase(Locale.ROOT)).normalizado();
        return metodo;
    }

    private BigDecimal money(BigDecimal value) {
        if (value == null) throw new DomainException("El monto es obligatorio.");
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizarKey(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private String hashPayload(Object payload) {
        try {
            return sha256(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new DomainException("No se pudo procesar el payload de venta.", e);
        }
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new DomainException("No se pudo calcular hash de idempotencia.", e);
        }
    }

    private void audit(PaymentIntentJpaEntity payment,
                       String eventType,
                       String previousStatus,
                       String newStatus,
                       Long userId,
                       String userRole,
                       String reference,
                       String observation,
                       BigDecimal amount,
                       String providerEventId) {
        // Cada cambio de estado de pago/venta queda como evento append-only para auditoria operativa.
        PaymentAuditJpaEntity audit = new PaymentAuditJpaEntity();
        audit.setIdPayment(payment.getId());
        audit.setIdVenta(payment.getIdVenta());
        audit.setEventType(eventType);
        audit.setPreviousStatus(previousStatus);
        audit.setNewStatus(newStatus);
        audit.setUserId(userId);
        audit.setUserRole(userRole);
        audit.setMethod(payment.getMethod().name());
        audit.setAmount(amount);
        audit.setCurrency(payment.getCurrency());
        audit.setReference(reference);
        audit.setObservation(observation);
        audit.setProviderEventId(providerEventId);
        audit.setCreatedAt(LocalDateTime.now());
        auditRepository.save(audit);
    }

    private VentaResponseDTO ventaResponse(VentaJpaEntity venta, PaymentIntentJpaEntity payment) {
        List<VentaResponseDTO.DetalleVentaResponseDTO> detalles = venta.getDetalles().stream()
                .map(detalle -> new VentaResponseDTO.DetalleVentaResponseDTO(
                        detalle.getId(),
                        detalle.getIdVariante(),
                        detalle.getCantidad(),
                        detalle.getPrecioUnitario(),
                        detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(detalle.getCantidad()))
                ))
                .toList();
        BigDecimal total = detalles.stream()
                .map(VentaResponseDTO.DetalleVentaResponseDTO::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<PaymentAuditResponseDTO> trazabilidad = payment == null ? List.of()
                : auditRepository.findByIdPaymentOrderByCreatedAtAsc(payment.getId()).stream()
                .map(item -> new PaymentAuditResponseDTO(
                        item.getEventType(),
                        item.getPreviousStatus(),
                        item.getNewStatus(),
                        item.getUserId(),
                        item.getUserRole(),
                        item.getReference(),
                        item.getObservation(),
                        item.getCreatedAt()
                ))
                .toList();
        return new VentaResponseDTO(
                venta.getId(),
                venta.getIdUsuario(),
                venta.getEstado().normalizado(),
                venta.getMetodoPago(),
                venta.getFecha(),
                detalles,
                total,
                payment == null ? null : paymentResponse(payment),
                trazabilidad
        );
    }

    private PaymentIntentResponseDTO paymentResponse(PaymentIntentJpaEntity payment) {
        return new PaymentIntentResponseDTO(
                payment.getId(),
                payment.getIdVenta(),
                payment.getMethod(),
                payment.getProvider(),
                payment.getProviderReference(),
                payment.getAmountDue(),
                payment.getAmountReceived(),
                payment.getChangeAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getPaymentReference(),
                payment.getExpiresAt(),
                payment.getCreatedAt(),
                payment.getConfirmedAt()
        );
    }
}
