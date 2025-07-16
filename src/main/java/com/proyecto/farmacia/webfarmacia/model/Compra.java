package com.proyecto.farmacia.webfarmacia.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "compras")
public class Compra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "compra_id")
    private Long compraId;

    private Long proveedorId;

    private LocalDateTime fecha;

    private BigDecimal total;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<DetalleCompra> detalles;

    // Getters y Setters
    public Long getCompraId() {
        return compraId;
    }
    public void setCompraId(Long compraId) {
        this.compraId = compraId;
    }
    public Long getProveedorId() {
        return proveedorId;
    }
    public void setProveedorId(Long proveedorId) {
        this.proveedorId = proveedorId;
    }
    public LocalDateTime getFecha() {
        return fecha;
    }
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
    public BigDecimal getTotal() {
        return total;
    }
    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<DetalleCompra> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleCompra> detalles) { this.detalles = detalles; }
} 