package com.omgmoda.sistema_inventario.producto.infraestructura.entities;

import jakarta.persistence.*;

/**
 * Entidad JPA que mapea la tabla PRODUCTO (3FN).
 * Completamente separada de la entidad de dominio Producto.
 * Solo la capa de infraestructura conoce esta clase.
 */
@Entity
@Table(name = "PRODUCTO")
public class ProductoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "categoria", nullable = false, length = 50)
    private String categoria;

    @Column(name = "marca", nullable = false, length = 50)
    private String marca;

    @Column(name = "imagen_url", length = 500)
    private String imageUrl;

    // Constructor vacío requerido por JPA
    public ProductoJpaEntity() {}

    public ProductoJpaEntity(String nombre, String categoria, String marca) {
        this(nombre, categoria, marca, null);
    }

    public ProductoJpaEntity(String nombre, String categoria, String marca, String imageUrl) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.marca = marca;
        this.imageUrl = imageUrl;
    }

    // ── Getters y Setters ──────────────────────────────────────────────────────

    public Long getId()                   { return id; }
    public void setId(Long id)            { this.id = id; }
    public String getNombre()             { return nombre; }
    public void setNombre(String nombre)  { this.nombre = nombre; }
    public String getCategoria()          { return categoria; }
    public void setCategoria(String c)    { this.categoria = c; }
    public String getMarca()              { return marca; }
    public void setMarca(String marca)    { this.marca = marca; }
    public String getImageUrl()           { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
