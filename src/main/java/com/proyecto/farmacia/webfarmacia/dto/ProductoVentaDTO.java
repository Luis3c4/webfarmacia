package com.proyecto.farmacia.webfarmacia.dto;

public class ProductoVentaDTO {
    private Long productoId;
    private String producto;
    private String category;
    private Double turnOver;
    private Double increaseBy;

    public ProductoVentaDTO() {}

    public ProductoVentaDTO(Long productoId, String producto, String category, Double turnOver, Double increaseBy) {
        this.productoId = productoId;
        this.producto = producto;
        this.category = category;
        this.turnOver = turnOver;
        this.increaseBy = increaseBy;
    }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public String getProducto() { return producto; }
    public void setProducto(String producto) { this.producto = producto; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Double getTurnOver() { return turnOver; }
    public void setTurnOver(Double turnOver) { this.turnOver = turnOver; }
    public Double getIncreaseBy() { return increaseBy; }
    public void setIncreaseBy(Double increaseBy) { this.increaseBy = increaseBy; }
} 