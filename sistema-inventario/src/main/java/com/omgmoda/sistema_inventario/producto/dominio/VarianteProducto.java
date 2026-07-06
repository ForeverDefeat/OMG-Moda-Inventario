package com.omgmoda.sistema_inventario.producto.dominio;

import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;
import com.omgmoda.sistema_inventario.shared.dominio.valueobjects.StockStatus;

import java.math.BigDecimal;

/**
 * Entity principal de inventario.
 * Cada instancia representa una SKU única: producto + talla + color + material.
 * Encapsula toda la lógica de control de stock.
 */
public class VarianteProducto {

    private Long id;
    private Producto producto;
    private String sku;
    private String talla;
    private String color;
    private String material;
    private BigDecimal precioCosto;
    private BigDecimal precioVenta;
    private int stockActual;
    private int stockReservado;
    private int stockMinimo;

    // Constructor usado por Producto.crearVariante()
    VarianteProducto(Producto producto,
                     String sku,
                     String talla,
                     String color,
                     String material,
                     BigDecimal precioCosto,
                     BigDecimal precioVenta) {
        this.producto = producto;
        this.sku = sku;
        this.talla = talla;
        this.color = color;
        this.material = material;
        this.precioCosto = precioCosto;
        this.precioVenta = precioVenta;
        this.stockActual = 0;
        this.stockReservado = 0;
        this.stockMinimo = 5;
    }

    public VarianteProducto(Long id,
                             Producto producto,
                             String talla,
                             String color,
                             String material,
                             BigDecimal precioCosto,
                             BigDecimal precioVenta,
                             int stockActual,
                             int stockMinimo) {
        this(id, producto, "SKU-" + id, talla, color, material, precioCosto, precioVenta, stockActual, 0, stockMinimo);
    }
    // Constructor para reconstitución desde persistencia
    public VarianteProducto(Long id,
                             Producto producto,
                             String sku,
                             String talla,
                             String color,
                             String material,
                             BigDecimal precioCosto,
                             BigDecimal precioVenta,
                             int stockActual,
                             int stockMinimo) {
        this(id, producto, sku, talla, color, material, precioCosto, precioVenta, stockActual, 0, stockMinimo);
    }

    public VarianteProducto(Long id,
                             Producto producto,
                             String sku,
                             String talla,
                             String color,
                             String material,
                             BigDecimal precioCosto,
                             BigDecimal precioVenta,
                             int stockActual,
                             int stockReservado,
                             int stockMinimo) {
        this.id = id;
        this.producto = producto;
        this.sku = sku;
        this.talla = talla;
        this.color = color;
        this.material = material;
        this.precioCosto = precioCosto;
        this.precioVenta = precioVenta;
        this.stockActual = stockActual;
        this.stockReservado = stockReservado;
        this.stockMinimo = stockMinimo;
    }

    // ── Lógica de negocio ──────────────────────────────────────────────────────

    /**
     * Incrementa el stock al recibir mercadería.
     * @param cantidad debe ser mayor a cero.
     */
    public void registrarEntrada(int cantidad) {
        if (cantidad <= 0)
            throw new DomainException("La cantidad de entrada debe ser mayor a cero.");
        this.stockActual += cantidad;
    }

    /**
     * Decrementa el stock al concretar una venta.
     * Valida disponibilidad antes de descontar.
     * @param cantidad debe ser mayor a cero y no superar el stock disponible.
     */
    public void registrarSalida(int cantidad) {
        if (cantidad <= 0)
            throw new DomainException("La cantidad de salida debe ser mayor a cero.");
        if (cantidad > stockDisponible())
            throw new DomainException(
                    "Stock insuficiente. Disponible: " + stockDisponible() +
                    ", solicitado: " + cantidad + "."
            );
        this.stockActual -= cantidad;
    }

    public void reservarStock(int cantidad) {
        if (cantidad <= 0)
            throw new DomainException("La cantidad a reservar debe ser mayor a cero.");
        if (cantidad > stockDisponible())
            throw new DomainException(
                    "Stock insuficiente. Disponible: " + stockDisponible() +
                    ", solicitado: " + cantidad + "."
            );
        this.stockReservado += cantidad;
    }

    public void consumirReserva(int cantidad) {
        if (cantidad <= 0)
            throw new DomainException("La cantidad a consumir debe ser mayor a cero.");
        if (cantidad > this.stockReservado)
            throw new DomainException("No hay stock reservado suficiente para consumir.");
        if (cantidad > this.stockActual)
            throw new DomainException("No hay stock actual suficiente para completar la venta.");
        this.stockReservado -= cantidad;
        this.stockActual -= cantidad;
    }

    public void liberarReserva(int cantidad) {
        if (cantidad <= 0)
            throw new DomainException("La cantidad a liberar debe ser mayor a cero.");
        if (cantidad > this.stockReservado)
            throw new DomainException("No hay stock reservado suficiente para liberar.");
        this.stockReservado -= cantidad;
    }

    /**
     * Corrige el stock manualmente (auditoría física).
     * @param cantidad nuevo valor absoluto de stock (no diferencia).
     * @param motivo   descripción obligatoria del ajuste.
     */
    public void ajustarStock(int cantidad, String motivo) {
        if (cantidad < 0)
            throw new DomainException("El stock ajustado no puede ser negativo.");
        if (motivo == null || motivo.isBlank())
            throw new DomainException("El motivo del ajuste es obligatorio.");
        this.stockActual = cantidad;
    }

    /**
     * Indica si el stock está en o por debajo del mínimo definido.
     * Usado para disparar la alerta StockMinimoAlcanzado.
     */
    public boolean esBajoStock() {
        return this.stockActual <= this.stockMinimo;
    }

    /**
     * Retorna el estado de stock como value object.
     */
    public StockStatus getStockStatus() {
        if (stockActual == 0) return StockStatus.AGOTADO;
        if (esBajoStock())    return StockStatus.BAJO_STOCK;
        return StockStatus.NORMAL;
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public Long getId()              { return id; }
    public Producto getProducto()    { return producto; }
    public String getSku()           { return sku; }
    public String getTalla()         { return talla; }
    public String getColor()         { return color; }
    public String getMaterial()      { return material; }
    public BigDecimal getPrecioCosto()  { return precioCosto; }
    public BigDecimal getPrecioVenta()  { return precioVenta; }
    public int getStockActual()      { return stockActual; }
    public int getStockReservado()   { return stockReservado; }
    public int stockDisponible()     { return stockActual - stockReservado; }
    public int getStockMinimo()      { return stockMinimo; }

    public void setId(Long id)       { this.id = id; }
}
