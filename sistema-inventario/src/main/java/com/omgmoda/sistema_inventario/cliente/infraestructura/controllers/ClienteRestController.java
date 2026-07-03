package com.omgmoda.sistema_inventario.cliente.infraestructura.controllers;

import com.omgmoda.sistema_inventario.cliente.aplicacion.dto.ClienteResponseDTO;
import com.omgmoda.sistema_inventario.cliente.aplicacion.dto.CrearClienteDTO;
import com.omgmoda.sistema_inventario.cliente.aplicacion.ports.IClienteUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
@Tag(name = "Clientes", description = "Gestion de clientes y segmentacion comercial.")
@SecurityRequirement(name = "bearer-jwt")
public class ClienteRestController {

    private final IClienteUseCase clienteUseCase;

    public ClienteRestController(IClienteUseCase clienteUseCase) {
        this.clienteUseCase = clienteUseCase;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar clientes", description = "Retorna los clientes registrados. Requiere rol ADMIN.")
    public ResponseEntity<List<ClienteResponseDTO>> listar() {
        return ResponseEntity.ok(clienteUseCase.listar());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear cliente", description = "Registra un cliente nuevo. Requiere rol ADMIN.")
    public ResponseEntity<ClienteResponseDTO> crear(@Valid @RequestBody CrearClienteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteUseCase.crear(dto));
    }
}
