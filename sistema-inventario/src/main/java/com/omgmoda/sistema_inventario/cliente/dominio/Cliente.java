package com.omgmoda.sistema_inventario.cliente.dominio;

import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Cliente {

    private Long id;
    private String nombre;
    private String correo;
    private String telefono;
    private String segmento;
    private BigDecimal totalCompras;
    private LocalDate ultimaCompra;

    public Cliente(Long id,
                   String nombre,
                   String correo,
                   String telefono,
                   String segmento,
                   BigDecimal totalCompras,
                   LocalDate ultimaCompra) {
        validar(nombre, correo, telefono, segmento, totalCompras);
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.segmento = segmento;
        this.totalCompras = totalCompras;
        this.ultimaCompra = ultimaCompra;
    }

    private void validar(String nombre,
                         String correo,
                         String telefono,
                         String segmento,
                         BigDecimal totalCompras) {
        if (nombre == null || nombre.isBlank())
            throw new DomainException("El nombre del cliente es obligatorio.");
        if (correo == null || correo.isBlank())
            throw new DomainException("El correo del cliente es obligatorio.");
        if (!correo.contains("@"))
            throw new DomainException("El correo del cliente no tiene un formato valido.");
        if (telefono == null || telefono.isBlank())
            throw new DomainException("El telefono del cliente es obligatorio.");
        if (segmento == null || segmento.isBlank())
            throw new DomainException("El segmento del cliente es obligatorio.");
        if (!segmento.equals("VIP") && !segmento.equals("Frecuente") && !segmento.equals("Nuevo"))
            throw new DomainException("El segmento del cliente no es valido.");
        if (totalCompras == null || totalCompras.signum() < 0)
            throw new DomainException("El total de compras no puede ser negativo.");
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getSegmento() {
        return segmento;
    }

    public BigDecimal getTotalCompras() {
        return totalCompras;
    }

    public LocalDate getUltimaCompra() {
        return ultimaCompra;
    }
}
