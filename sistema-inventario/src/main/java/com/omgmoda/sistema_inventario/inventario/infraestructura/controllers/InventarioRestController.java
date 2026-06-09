package com.omgmoda.sistema_inventario.inventario.infraestructura.controllers;

import com.omgmoda.sistema_inventario.inventario.aplicacion.dto.MovimientoResponseDTO;
import com.omgmoda.sistema_inventario.inventario.aplicacion.dto.RegistrarAjusteDTO;
import com.omgmoda.sistema_inventario.inventario.aplicacion.dto.RegistrarEntradaDTO;
import com.omgmoda.sistema_inventario.inventario.aplicacion.ports.IRegistrarAjusteUseCase;
import com.omgmoda.sistema_inventario.inventario.aplicacion.ports.IRegistrarEntradaUseCase;
import com.omgmoda.sistema_inventario.usuario.infraestructura.security.UsuarioAutenticadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/movimientos")
@Tag(name = "Inventario", description = "Movimientos de stock: entradas y ajustes.")
@SecurityRequirement(name = "bearer-jwt")
public class InventarioRestController {

    private final IRegistrarEntradaUseCase registrarEntradaUseCase;
    private final IRegistrarAjusteUseCase registrarAjusteUseCase;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public InventarioRestController(IRegistrarEntradaUseCase registrarEntradaUseCase,
                                    IRegistrarAjusteUseCase registrarAjusteUseCase,
                                    UsuarioAutenticadoService usuarioAutenticadoService) {
        this.registrarEntradaUseCase = registrarEntradaUseCase;
        this.registrarAjusteUseCase = registrarAjusteUseCase;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @PostMapping("/entrada")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Registrar entrada de stock",
            description = "Incrementa el stock de una variante y registra un movimiento ENTRADA. El usuario se toma del JWT."
    )
    public ResponseEntity<MovimientoResponseDTO> registrarEntrada(
            @Valid @RequestBody RegistrarEntradaDTO dto) {
        Long idUsuario = usuarioAutenticadoService.obtenerIdUsuarioActual();
        MovimientoResponseDTO resultado = registrarEntradaUseCase.registrar(dto, idUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    @PostMapping("/ajuste")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Registrar ajuste de stock",
            description = "Establece el stock absoluto de una variante y registra un movimiento AJUSTE. El usuario se toma del JWT."
    )
    public ResponseEntity<MovimientoResponseDTO> registrarAjuste(
            @Valid @RequestBody RegistrarAjusteDTO dto) {
        Long idUsuario = usuarioAutenticadoService.obtenerIdUsuarioActual();
        MovimientoResponseDTO resultado = registrarAjusteUseCase.ajustar(dto, idUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }
}
