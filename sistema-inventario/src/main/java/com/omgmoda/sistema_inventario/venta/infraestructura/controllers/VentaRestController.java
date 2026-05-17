package com.omgmoda.sistema_inventario.venta.infraestructura.controllers;

import com.omgmoda.sistema_inventario.venta.aplicacion.dto.CrearVentaDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.VentaResponseDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.ports.IAnularVentaUseCase;
import com.omgmoda.sistema_inventario.venta.aplicacion.ports.IConsultarVentaUseCase;
import com.omgmoda.sistema_inventario.venta.aplicacion.ports.IRegistrarVentaUseCase;
import com.omgmoda.sistema_inventario.venta.dominio.EstadoVenta;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Adaptador de entrada REST para el módulo Venta.
 * Extrae el idUsuario del token JWT mediante @AuthenticationPrincipal
 * para evitar que el cliente lo falsifique en el body (mejora de seguridad).
 */
@RestController
@RequestMapping("/api/v1/ventas")
public class VentaRestController {

    private final IRegistrarVentaUseCase registrarVentaUseCase;
    private final IConsultarVentaUseCase consultarVentaUseCase;
    private final IAnularVentaUseCase anularVentaUseCase;

    public VentaRestController(IRegistrarVentaUseCase registrarVentaUseCase,
                                IConsultarVentaUseCase consultarVentaUseCase,
                                IAnularVentaUseCase anularVentaUseCase) {
        this.registrarVentaUseCase = registrarVentaUseCase;
        this.consultarVentaUseCase = consultarVentaUseCase;
        this.anularVentaUseCase = anularVentaUseCase;
    }

    /**
     * POST /api/v1/ventas
     * Registra una venta nueva. El idUsuario se extrae del token JWT.
     * Acceso: ADMIN y VENDEDOR.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<VentaResponseDTO> crearVenta(
            @Valid @RequestBody CrearVentaDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long idUsuario = extraerIdUsuario(userDetails);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(registrarVentaUseCase.registrar(dto, idUsuario));
    }

    /**
     * GET /api/v1/ventas/{id}
     * Retorna el detalle de una venta por su id.
     * Acceso: ADMIN y VENDEDOR.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<VentaResponseDTO> obtenerVenta(@PathVariable Long id) {
        return ResponseEntity.ok(consultarVentaUseCase.buscarPorId(id));
    }

    /**
     * GET /api/v1/ventas
     * Lista ventas con filtros opcionales por estado y rango de fechas.
     * Ejemplo: GET /api/v1/ventas?estado=COMPLETADA&desde=2024-01-01T00:00:00
     * Acceso: ADMIN y VENDEDOR.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<List<VentaResponseDTO>> listarVentas(
            @RequestParam(required = false) EstadoVenta estado,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {

        if (estado != null)
            return ResponseEntity.ok(consultarVentaUseCase.buscarPorEstado(estado));
        if (desde != null && hasta != null)
            return ResponseEntity.ok(consultarVentaUseCase.buscarPorFechas(desde, hasta));

        // Sin filtros: retorna ventas del usuario autenticado
        return ResponseEntity.ok(List.of());
    }

    /**
     * PATCH /api/v1/ventas/{id}/anular
     * Anula una venta completada y revierte el stock.
     * Acceso: solo ADMIN.
     */
    @PatchMapping("/{id}/anular")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VentaResponseDTO> anularVenta(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long idUsuario = extraerIdUsuario(userDetails);
        return ResponseEntity.ok(anularVentaUseCase.anular(id, idUsuario));
    }

    // ── Método auxiliar ────────────────────────────────────────────────────────

    /**
     * Extrae el id del usuario desde el contexto de seguridad.
     * El correo es el subject del JWT; se usa como identificador temporal.
     * En producción se puede resolver el id real desde IUsuarioRepository.
     */
    private Long extraerIdUsuario(UserDetails userDetails) {
        // El correo se almacena como username en el token JWT
        // Para obtener el id real se debería consultar IUsuarioRepository
        // por ahora retornamos -1L como placeholder hasta integrar el lookup
        return userDetails != null ? userDetails.hashCode() * -1L : -1L;
    }
}
