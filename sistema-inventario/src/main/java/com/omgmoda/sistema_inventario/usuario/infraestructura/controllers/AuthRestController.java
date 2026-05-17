package com.omgmoda.sistema_inventario.usuario.infraestructura.controllers;

import com.omgmoda.sistema_inventario.usuario.aplicacion.dto.AuthResponseDTO;
import com.omgmoda.sistema_inventario.usuario.aplicacion.dto.LoginDTO;
import com.omgmoda.sistema_inventario.usuario.aplicacion.ports.IAutenticarUseCase;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Adaptador de entrada REST para autenticación.
 * Expone el endpoint de login y delega en IAutenticarUseCase.
 * Es el único endpoint público del sistema (sin @PreAuthorize).
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthRestController {

    private final IAutenticarUseCase autenticarUseCase;

    public AuthRestController(IAutenticarUseCase autenticarUseCase) {
        this.autenticarUseCase = autenticarUseCase;
    }

    /**
     * POST /api/v1/auth/login
     * Recibe correo y contraseña, retorna token JWT si las credenciales son válidas.
     * Acceso: público (sin autenticación previa).
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginDTO dto) {
        return ResponseEntity.ok(autenticarUseCase.autenticar(dto));
    }
}
