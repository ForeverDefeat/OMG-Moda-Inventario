package com.omgmoda.sistema_inventario.inventario.infraestructura.controllers;

import com.omgmoda.sistema_inventario.inventario.aplicacion.dto.MovimientoResponseDTO;
import com.omgmoda.sistema_inventario.inventario.aplicacion.dto.RegistrarMovimientoDTO;
import com.omgmoda.sistema_inventario.inventario.aplicacion.ports.IRegistrarAjusteUseCase;
import com.omgmoda.sistema_inventario.inventario.aplicacion.ports.IRegistrarEntradaUseCase;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Adaptador de entrada REST para el módulo Inventario.
 * Expone los endpoints de movimientos de stock y delega en los Input Ports.
 * No contiene lógica de negocio.
 */
@RestController
@RequestMapping("/api/v1/movimientos")
public class InventarioRestController {

    private final IRegistrarEntradaUseCase registrarEntradaUseCase;
    private final IRegistrarAjusteUseCase registrarAjusteUseCase;

    public InventarioRestController(IRegistrarEntradaUseCase registrarEntradaUseCase,
                                    IRegistrarAjusteUseCase registrarAjusteUseCase) {
        this.registrarEntradaUseCase = registrarEntradaUseCase;
        this.registrarAjusteUseCase = registrarAjusteUseCase;
    }

    /**
     * POST /api/v1/movimientos/entrada
     * Registra la recepción de mercadería e incrementa el stock (RF03, CU-06).
     * Acceso: solo ADMIN.
     */
    @PostMapping("/entrada")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovimientoResponseDTO> registrarEntrada(
            @Valid @RequestBody RegistrarMovimientoDTO dto) {
        MovimientoResponseDTO resultado = registrarEntradaUseCase.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    /**
     * POST /api/v1/movimientos/ajuste
     * Registra un ajuste manual de stock con motivo obligatorio (CU-08).
     * Acceso: solo ADMIN.
     */
    @PostMapping("/ajuste")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovimientoResponseDTO> registrarAjuste(
            @Valid @RequestBody RegistrarMovimientoDTO dto) {
        MovimientoResponseDTO resultado = registrarAjusteUseCase.ajustar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }
}
