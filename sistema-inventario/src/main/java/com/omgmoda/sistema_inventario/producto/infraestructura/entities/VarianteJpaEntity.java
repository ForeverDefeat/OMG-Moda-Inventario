package com.omgmoda.sistema_inventario.producto.infraestructura.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entidad JPA que mapea la tabla VARIANTE_PRODUCTO (3FN).
 * No contiene subtotal ni ningún campo derivado (cumple 3FN).
 */
@Entity
@Table(name = "VARIANTE_PRODUCTO", uniqueConstraints = {
        @UniqueConstraint(name = "uk_variante_producto_sku", columnNames = "sku")
})
public class VarianteJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_variante")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private ProductoJpaEntity producto;

    @Column(name = "sku", nullable = false, length = 40, unique = true)
    private String sku;

    @Column(name = "talla", nullable = false, length = 20)
    private String talla;

    @Column(name = "color", nullable = false, length = 30)
    private String color;

    @Column(name = "material", length = 50)
    private String material;

    @Column(name = "precio_costo", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioCosto;

    @Column(name = "precio_venta", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "stock_actual", nullable = false)
    private int stockActual = 0;

    @Column(name = "stock_reservado", nullable = false, columnDefinition = "int default 0")
    private int stockReservado = 0;

    @Column(name = "stock_minimo", nullable = false)
    private int stockMinimo = 5;

    // Constructor vacío requerido por JPA
    public VarianteJpaEntity() {}

    // ── Getters y Setters ──────────────────────────────────────────────────────

    public Long getId()                            { return id; }
    public void setId(Long id)                     { this.id = id; }
    public ProductoJpaEntity getProducto()         { return producto; }
    public void setProducto(ProductoJpaEntity p)   { this.producto = p; }
    public String getSku()                         { return sku; }
    public void setSku(String sku)                 { this.sku = sku; }
    public String getTalla()                       { return talla; }
    public void setTalla(String talla)             { this.talla = talla; }
    public String getColor()                       { return color; }
    public void setColor(String color)             { this.color = color; }
    public String getMaterial()                    { return material; }
    public void setMaterial(String material)       { this.material = material; }
    public BigDecimal getPrecioCosto()             { return precioCosto; }
    public void setPrecioCosto(BigDecimal pc)      { this.precioCosto = pc; }
    public BigDecimal getPrecioVenta()             { return precioVenta; }
    public void setPrecioVenta(BigDecimal pv)      { this.precioVenta = pv; }
    public int getStockActual()                    { return stockActual; }
    public void setStockActual(int stockActual)    { this.stockActual = stockActual; }
    public int getStockReservado()                 { return stockReservado; }
    public void setStockReservado(int stockReservado) { this.stockReservado = stockReservado; }
    public int getStockMinimo()                    { return stockMinimo; }
    public void setStockMinimo(int stockMinimo)    { this.stockMinimo = stockMinimo; }
}
