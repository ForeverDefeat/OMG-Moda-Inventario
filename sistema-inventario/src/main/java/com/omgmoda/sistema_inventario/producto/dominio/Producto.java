package com.omgmoda.sistema_inventario.producto.dominio;

import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate Root del módulo Producto.
 * Gestiona la creación de variantes y garantiza que ninguna
 * variante exista sin un producto padre válido.
 */
public class Producto {

    private Long id;
    private String nombre;
    private String categoria;
    private String marca;
    private String imageUrl;
    private final List<VarianteProducto> variantes = new ArrayList<>();

    // Constructor para reconstitución desde persistencia
    public Producto(Long id, String nombre, String categoria, String marca) {
        this(id, nombre, categoria, marca, null);
    }

    public Producto(Long id, String nombre, String categoria, String marca, String imageUrl) {
        validar(nombre, categoria, marca);
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.marca = marca;
        this.imageUrl = normalizarImageUrl(imageUrl);
    }

    // Constructor para creación nueva (sin id, lo asigna la BD)
    public Producto(String nombre, String categoria, String marca) {
        this(nombre, categoria, marca, null);
    }

    public Producto(String nombre, String categoria, String marca, String imageUrl) {
        validar(nombre, categoria, marca);
        this.nombre = nombre;
        this.categoria = categoria;
        this.marca = marca;
        this.imageUrl = normalizarImageUrl(imageUrl);
    }

    /**
     * Método de fábrica: única vía para crear una VarianteProducto.
     * Garantiza la invariante: no puede existir variante sin producto padre.
     */
    public VarianteProducto crearVariante(String talla,
                                          String color,
                                          String material,
                                          BigDecimal precioCosto,
                                          BigDecimal precioVenta) {
        return crearVariante(null, talla, color, material, precioCosto, precioVenta);
    }

    public VarianteProducto crearVariante(String sku,
                                          String talla,
                                          String color,
                                          String material,
                                          BigDecimal precioCosto,
                                          BigDecimal precioVenta) {
        if (talla == null || talla.isBlank())
            throw new DomainException("La talla de la variante no puede estar vacía.");
        if (color == null || color.isBlank())
            throw new DomainException("El color de la variante no puede estar vacío.");
        if (sku == null || sku.isBlank())
            throw new DomainException("El SKU de la variante no puede estar vacio.");
        if (precioCosto == null || precioCosto.compareTo(BigDecimal.ZERO) < 0)
            throw new DomainException("El precio de costo no puede ser negativo.");
        if (precioVenta == null || precioVenta.compareTo(BigDecimal.ZERO) <= 0)
            throw new DomainException("El precio de venta debe ser mayor a cero.");

        VarianteProducto variante = new VarianteProducto(
                this, sku, talla, color, material, precioCosto, precioVenta
        );
        variantes.add(variante);
        return variante;
    }

    // ── Validación interna ─────────────────────────────────────────────────────

    private void validar(String nombre, String categoria, String marca) {
        if (nombre == null || nombre.isBlank())
            throw new DomainException("El nombre del producto no puede estar vacío.");
        if (categoria == null || categoria.isBlank())
            throw new DomainException("La categoría no puede estar vacía.");
        if (marca == null || marca.isBlank())
            throw new DomainException("La marca no puede estar vacía.");
    }

    // ── Getters (sin setters: inmutabilidad controlada) ────────────────────────

    public void actualizarDatosVisuales(String nombre, String imageUrl) {
        validar(nombre, categoria, marca);
        this.nombre = nombre.trim();
        this.imageUrl = normalizarImageUrl(imageUrl);
    }

    private String normalizarImageUrl(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public String getMarca() { return marca; }
    public String getImageUrl() { return imageUrl; }
    public List<VarianteProducto> getVariantes() {
        return Collections.unmodifiableList(variantes);
    }

    // Usado solo por el adaptador JPA al reconstituir el agregado
    public void setId(Long id) { this.id = id; }
}
