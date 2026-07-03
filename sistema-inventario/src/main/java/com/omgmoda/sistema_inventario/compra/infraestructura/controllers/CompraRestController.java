package com.omgmoda.sistema_inventario.compra.infraestructura.controllers;

import com.omgmoda.sistema_inventario.compra.aplicacion.dto.CompraSugerenciaDTO;
import com.omgmoda.sistema_inventario.compra.aplicacion.usecases.CompraSugerenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/compras")
@Tag(name = "Compras", description = "Sugerencias de reposicion generadas desde inventario.")
@SecurityRequirement(name = "bearer-jwt")
public class CompraRestController {

    private final CompraSugerenciaService compraSugerenciaService;

    public CompraRestController(CompraSugerenciaService compraSugerenciaService) {
        this.compraSugerenciaService = compraSugerenciaService;
    }

    @GetMapping("/sugerencias")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Listar sugerencias de compra",
            description = "Retorna variantes con bajo stock y una cantidad sugerida de reposicion. Requiere rol ADMIN."
    )
    public ResponseEntity<List<CompraSugerenciaDTO>> listarSugerencias() {
        return ResponseEntity.ok(compraSugerenciaService.listarSugerencias());
    }
}
