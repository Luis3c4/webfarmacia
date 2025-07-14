package com.proyecto.farmacia.webfarmacia.dto;

import java.math.BigDecimal;

public class DetalleVentaDTO {
    private Long detalleId;
    private Long ventaId;
    private String productoNombre;
    private Long productoId;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal costeUnitario;
    private BigDecimal descuentos;

    // Constructor por defecto
    public DetalleVentaDTO() {}

    // Constructor con parámetros
    public DetalleVentaDTO(Long detalleId, Long ventaId, String productoNombre, Long productoId, 
                          int cantidad, BigDecimal precioUnitario, BigDecimal costeUnitario, BigDecimal descuentos) {
        this.detalleId = detalleId;
        this.ventaId = ventaId;
        this.productoNombre = productoNombre;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.costeUnitario = costeUnitario;
        this.descuentos = descuentos;
    }

    // Getters y Setters
    public Long getDetalleId() { return detalleId; }
    public void setDetalleId(Long detalleId) { this.detalleId = detalleId; }

    public Long getVentaId() { return ventaId; }
    public void setVentaId(Long ventaId) { this.ventaId = ventaId; }

    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getCosteUnitario() { return costeUnitario; }
    public void setCosteUnitario(BigDecimal costeUnitario) { this.costeUnitario = costeUnitario; }

    public BigDecimal getDescuentos() { return descuentos; }
    public void setDescuentos(BigDecimal descuentos) { this.descuentos = descuentos; }
} 