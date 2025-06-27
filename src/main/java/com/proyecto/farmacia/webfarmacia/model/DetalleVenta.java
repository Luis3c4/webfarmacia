package com.proyecto.farmacia.webfarmacia.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_venta")
public class DetalleVenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detalle_id")
    private Long detalleId;

    @ManyToOne
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    private int cantidad;

    @Column(name = "precio_unitario")
    private BigDecimal precioUnitario;

    @Column(name = "coste_unitario")
    private BigDecimal costeUnitario;

    private BigDecimal descuentos = BigDecimal.ZERO;

    // Getters y Setters
    public Long getDetalleId() {
        return detalleId;
    }
    public void setDetalleId(Long detalleId) {
        this.detalleId = detalleId;
    }
    public Venta getVenta() {
        return venta;
    }
    public void setVenta(Venta venta) {
        this.venta = venta;
    }
    public Producto getProducto() {
        return producto;
    }
    public void setProducto(Producto producto) {
        this.producto = producto;
    }
    public int getCantidad() {
        return cantidad;
    }
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
    public BigDecimal getCosteUnitario() {
        return costeUnitario;
    }
    public void setCosteUnitario(BigDecimal costeUnitario) {
        this.costeUnitario = costeUnitario;
    }
    public BigDecimal getDescuentos() {
        return descuentos;
    }
    public void setDescuentos(BigDecimal descuentos) {
        this.descuentos = descuentos;
    }
} 