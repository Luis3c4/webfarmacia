package com.proyecto.farmacia.webfarmacia.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_compra")
public class DetalleCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detalle_id")
    private Long detalleId;

    @ManyToOne
    @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    private int cantidad;

    @Column(name = "coste_unitario")
    private BigDecimal costeUnitario;

    private BigDecimal descuentos;

    // Getters y Setters
    public Long getDetalleId() {
        return detalleId;
    }
    public void setDetalleId(Long detalleId) {
        this.detalleId = detalleId;
    }
    public Compra getCompra() {
        return compra;
    }
    public void setCompra(Compra compra) {
        this.compra = compra;
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