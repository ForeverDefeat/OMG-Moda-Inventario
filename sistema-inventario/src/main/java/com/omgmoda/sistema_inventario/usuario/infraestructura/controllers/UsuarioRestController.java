package com.omgmoda.sistema_inventario.usuario.infraestructura.controllers;

import com.omgmoda.sistema_inventario.usuario.aplicacion.dto.ActualizarRolUsuarioDTO;
import com.omgmoda.sistema_inventario.usuario.aplicacion.dto.CrearUsuarioDTO;
import com.omgmoda.sistema_inventario.usuario.aplicacion.dto.ResetearContraseniaDTO;
import com.omgmoda.sistema_inventario.usuario.aplicacion.dto.UsuarioResponseDTO;
import com.omgmoda.sistema_inventario.usuario.aplicacion.usecases.UsuarioAdminService;
import com.omgmoda.sistema_inventario.usuario.infraestructura.security.UsuarioAutenticadoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioRestController {

    private final UsuarioAdminService usuarioAdminService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public UsuarioRestController(UsuarioAdminService usuarioAdminService,
                                 UsuarioAutenticadoService usuarioAutenticadoService) {
        this.usuarioAdminService = usuarioAdminService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        return ResponseEntity.ok(usuarioAdminService.listar());
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> crear(@Valid @RequestBody CrearUsuarioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioAdminService.crear(dto));
    }

    @PutMapping("/{id}/rol")
    public ResponseEntity<UsuarioResponseDTO> cambiarRol(@PathVariable Long id,
                                                         @Valid @RequestBody ActualizarRolUsuarioDTO dto) {
        Long idUsuarioActual = usuarioAutenticadoService.obtenerIdUsuarioActual();
        return ResponseEntity.ok(usuarioAdminService.cambiarRol(id, dto.rol(), idUsuarioActual));
    }

    @PutMapping("/{id}/contrasenia")
    public ResponseEntity<UsuarioResponseDTO> resetearContrasenia(@PathVariable Long id,
                                                                  @Valid @RequestBody ResetearContraseniaDTO dto) {
        return ResponseEntity.ok(usuarioAdminService.resetearContrasenia(id, dto.nuevaContrasenia()));
    }

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<UsuarioResponseDTO> desactivar(@PathVariable Long id) {
        Long idUsuarioActual = usuarioAutenticadoService.obtenerIdUsuarioActual();
        return ResponseEntity.ok(usuarioAdminService.desactivar(id, idUsuarioActual));
    }

    @PutMapping("/{id}/reactivar")
    public ResponseEntity<UsuarioResponseDTO> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioAdminService.reactivar(id));
    }
}
