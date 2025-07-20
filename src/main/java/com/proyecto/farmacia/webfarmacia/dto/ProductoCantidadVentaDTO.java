package com.proyecto.farmacia.webfarmacia.dto;

public class ProductoCantidadVentaDTO {
    private Long productoId;
    private String producto;
    private String category;
    private Integer cantidadVendida;
    private Double valorTotal;

    public ProductoCantidadVentaDTO() {}

    public ProductoCantidadVentaDTO(Long productoId, String producto, String category, Integer cantidadVendida, Double valorTotal) {
        this.productoId = productoId;
        this.producto = producto;
        this.category = category;
        this.cantidadVendida = cantidadVendida;
        this.valorTotal = valorTotal;
    }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    
    public String getProducto() { return producto; }
    public void setProducto(String producto) { this.producto = producto; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Integer getCantidadVendida() { return cantidadVendida; }
    public void setCantidadVendida(Integer cantidadVendida) { this.cantidadVendida = cantidadVendida; }
    
    public Double getValorTotal() { return valorTotal; }
    public void setValorTotal(Double valorTotal) { this.valorTotal = valorTotal; }
} 