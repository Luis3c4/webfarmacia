package com.proyecto.farmacia.webfarmacia.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "producto_id")
    private Long productoId;

    @Column(name = "nombre", length = 255, nullable = false)
    private String nombre;
    
    @Column(name = "descripcion", length = 500)
    private String descripcion;
    
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio_unitario;
    
    @Column(name = "cantidad_ingresada", nullable = false)
    private int cantidadIngresada;
    
    @Column(name = "stock", nullable = false)
    private int stock;
    
    @Column(name = "categoria", length = 100)
    private String categoria;
    
    @Column(name = "coste_unitario", precision = 10, scale = 2)
    private BigDecimal costeUnitario;
    
    @Column(name = "stock_status", length = 20)
    private String stockStatus;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "img_url", length = 512)
    @Size(max = 512)
    private String imgUrl;

    @Column(name = "supabase_id", length = 255)
    private String supabaseId;

    @Column(name = "fecha_producto")
    private LocalDate fechaProducto;

    @Column(name = "descuento", precision = 5, scale = 2)
    private BigDecimal descuento;

    @Column(name = "metodo_entrega", length = 50)
    private String metodoEntrega;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructor por defecto
    public Producto() {
        this.precio_unitario = BigDecimal.ZERO;
        this.costeUnitario = BigDecimal.ZERO;
        this.descuento = BigDecimal.ZERO;
        this.cantidadIngresada = 0;
        this.stock = 0;
        this.activo = true;
        this.stockStatus = "OUT_OF_STOCK";
        this.metodoEntrega = "ambos";
        this.fechaProducto = LocalDate.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio_unitario() {
        return precio_unitario != null ? precio_unitario : BigDecimal.ZERO;
    }

    public void setPrecio_unitario(BigDecimal precio_unitario) {
        this.precio_unitario = precio_unitario;
    }

    // Getter temporal para compatibilidad con el frontend
    public double getPrecio() {
        return precio_unitario != null ? precio_unitario.doubleValue() : 0.0;
    }

    public void setPrecio(double precio) {
        this.precio_unitario = BigDecimal.valueOf(precio);
    }

    public int getCantidadIngresada() {
        return cantidadIngresada;
    }

    public void setCantidadIngresada(int cantidadIngresada) {
        this.cantidadIngresada = cantidadIngresada;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
        // Actualizar automáticamente el estado del stock solo si no se ha establecido manualmente
        if (this.stockStatus == null || this.stockStatus.isEmpty()) {
            if (stock <= 0) {
                this.stockStatus = "OUT_OF_STOCK";
            } else if (stock <= 10) {
                this.stockStatus = "LOW_STOCK";
            } else {
                this.stockStatus = "IN_STOCK";
            }
        }
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus) {
        // Validar que el stockStatus sea uno de los valores permitidos
        if (stockStatus != null && !stockStatus.isEmpty()) {
            if (!stockStatus.equals("IN_STOCK") && 
                !stockStatus.equals("OUT_OF_STOCK") && 
                !stockStatus.equals("LOW_STOCK")) {
                throw new IllegalArgumentException("stockStatus debe ser IN_STOCK, OUT_OF_STOCK o LOW_STOCK");
            }
        }
        this.stockStatus = stockStatus;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getCosteUnitario() {
        return costeUnitario != null ? costeUnitario : BigDecimal.ZERO;
    }

    public void setCosteUnitario(BigDecimal costeUnitario) {
        this.costeUnitario = costeUnitario;
    }
    
    // Getter para mantener consistencia con el estilo de nomenclatura
    public double getCoste_unitario() {
        return costeUnitario != null ? costeUnitario.doubleValue() : 0.0;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    public String getSupabaseId() {
        return supabaseId;
    }
    
    public void setSupabaseId(String supabaseId) {
        this.supabaseId = supabaseId;
    }
    
    public LocalDate getFechaProducto() {
        return fechaProducto;
    }
    
    public void setFechaProducto(LocalDate fechaProducto) {
        this.fechaProducto = fechaProducto;
    }
    
    public BigDecimal getDescuento() {
        return descuento != null ? descuento : BigDecimal.ZERO;
    }
    
    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
    }
    
    // Getter temporal para compatibilidad con el frontend
    public double getDescuentoDouble() {
        return descuento != null ? descuento.doubleValue() : 0.0;
    }
    
    public void setDescuentoDouble(double descuento) {
        this.descuento = BigDecimal.valueOf(descuento);
    }
    
    public String getMetodoEntrega() {
        return metodoEntrega;
    }
    
    public void setMetodoEntrega(String metodoEntrega) {
        this.metodoEntrega = metodoEntrega;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Producto{" +
                "productoId=" + productoId +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", precio_unitario=" + precio_unitario +
                ", cantidadIngresada=" + cantidadIngresada +
                ", stock=" + stock +
                ", categoria='" + categoria + '\'' +
                ", stockStatus='" + stockStatus + '\'' +
                ", activo=" + activo +
                ", metodoEntrega='" + metodoEntrega + '\'' +
                '}';
    }
}