package com.omgmoda.sistema_inventario.producto.infraestructura.controllers;

import com.omgmoda.sistema_inventario.producto.aplicacion.dto.ActualizarProductoDTO;
import com.omgmoda.sistema_inventario.producto.aplicacion.dto.CrearProductoDTO;
import com.omgmoda.sistema_inventario.producto.aplicacion.dto.VarianteResponseDTO;
import com.omgmoda.sistema_inventario.producto.aplicacion.ports.IActualizarProductoUseCase;
import com.omgmoda.sistema_inventario.producto.aplicacion.ports.IBuscarVariantesUseCase;
import com.omgmoda.sistema_inventario.producto.aplicacion.ports.IRegistrarProductoUseCase;
import com.omgmoda.sistema_inventario.producto.infraestructura.storage.ProductoImageStorageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Adaptador de entrada REST para el módulo Producto.
 * Expone los endpoints HTTP y delega en los Input Ports.
 * No contiene lógica de negocio.
 */
@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "Gestion de productos, variantes y consulta de stock.")
@SecurityRequirement(name = "bearer-jwt")
public class ProductoRestController {

    private final IRegistrarProductoUseCase registrarProductoUseCase;
    private final IActualizarProductoUseCase actualizarProductoUseCase;
    private final IBuscarVariantesUseCase buscarVariantesUseCase;
    private final ProductoImageStorageService imageStorageService;

    public ProductoRestController(IRegistrarProductoUseCase registrarProductoUseCase,
                                  IActualizarProductoUseCase actualizarProductoUseCase,
                                  IBuscarVariantesUseCase buscarVariantesUseCase,
                                  ProductoImageStorageService imageStorageService) {
        this.registrarProductoUseCase = registrarProductoUseCase;
        this.actualizarProductoUseCase = actualizarProductoUseCase;
        this.buscarVariantesUseCase = buscarVariantesUseCase;
        this.imageStorageService = imageStorageService;
    }

    /**
     * POST /api/v1/productos
     * Registra un producto nuevo con sus variantes iniciales.
     * Acceso: solo ADMIN.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Crear producto con variantes",
            description = "Registra un producto nuevo y sus variantes iniciales. Requiere rol ADMIN."
    )
    public ResponseEntity<List<VarianteResponseDTO>> crearProducto(
            @Valid @RequestBody CrearProductoDTO dto) {
        List<VarianteResponseDTO> resultado = registrarProductoUseCase.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Crear producto con imagen subida",
            description = "Registra un producto nuevo y guarda una imagen opcional en uploads/productos. Requiere rol ADMIN."
    )
    public ResponseEntity<List<VarianteResponseDTO>> crearProductoConImagen(
            @Valid @RequestPart("producto") CrearProductoDTO dto,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {
        String imageUrl = imageStorageService.store(imagen);
        CrearProductoDTO dtoConImagen = new CrearProductoDTO(
                dto.nombre(),
                dto.categoria(),
                dto.marca(),
                imageUrl != null ? imageUrl : dto.imageUrl(),
                dto.variantes()
        );
        List<VarianteResponseDTO> resultado = registrarProductoUseCase.registrar(dtoConImagen);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    @PutMapping(path = "/{idProducto}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Actualizar nombre e imagen de producto",
            description = "Actualiza datos visuales del producto y retorna sus variantes. Requiere rol ADMIN."
    )
    public ResponseEntity<List<VarianteResponseDTO>> actualizarProducto(
            @PathVariable Long idProducto,
            @Valid @RequestBody ActualizarProductoDTO dto) {
        return ResponseEntity.ok(actualizarProductoUseCase.actualizar(idProducto, dto));
    }

    @PutMapping(path = "/{idProducto}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Actualizar producto con imagen subida",
            description = "Actualiza nombre e imagen del producto usando una imagen subida o URL. Requiere rol ADMIN."
    )
    public ResponseEntity<List<VarianteResponseDTO>> actualizarProductoConImagen(
            @PathVariable Long idProducto,
            @Valid @RequestPart("producto") ActualizarProductoDTO dto,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {
        String imageUrl = imageStorageService.store(imagen);
        ActualizarProductoDTO dtoConImagen = new ActualizarProductoDTO(
                dto.nombre(),
                imageUrl != null ? imageUrl : dto.imageUrl()
        );
        return ResponseEntity.ok(actualizarProductoUseCase.actualizar(idProducto, dtoConImagen));
    }

    /**
     * GET /api/v1/productos/variantes
     * Busca variantes con filtros opcionales por talla, color y categoría.
     * Acceso: ADMIN y VENDEDOR.
     * Ejemplo: GET /api/v1/productos/variantes?talla=M&color=Negro&categoria=Camisas
     */
    @GetMapping("/variantes")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @Operation(
            summary = "Buscar variantes",
            description = "Consulta variantes por filtros opcionales de talla, color y categoria."
    )
    public ResponseEntity<List<VarianteResponseDTO>> buscarVariantes(
            @Parameter(description = "Talla de la variante. Ejemplo: M")
            @RequestParam(required = false) String talla,
            @Parameter(description = "Color de la variante. Ejemplo: Negro")
            @RequestParam(required = false) String color,
            @Parameter(description = "Categoria del producto. Ejemplo: Camisas")
            @RequestParam(required = false) String categoria,
            @Parameter(description = "SKU de la variante. Ejemplo: CW-CAMISA-OXFORD-M-AZUL-01")
            @RequestParam(required = false) String sku) {
        return ResponseEntity.ok(buscarVariantesUseCase.buscar(talla, color, categoria, sku));
    }

    /**
     * GET /api/v1/productos/variantes/bajo-stock
     * Retorna todas las variantes con stock en o por debajo del mínimo (RF09).
     * Acceso: solo ADMIN.
     */
    @GetMapping("/variantes/bajo-stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Listar variantes con bajo stock",
            description = "Retorna variantes cuyo stock actual esta en o por debajo del stock minimo. Requiere rol ADMIN."
    )
    public ResponseEntity<List<VarianteResponseDTO>> buscarBajoStock() {
        return ResponseEntity.ok(buscarVariantesUseCase.buscarBajoStock());
    }
}
