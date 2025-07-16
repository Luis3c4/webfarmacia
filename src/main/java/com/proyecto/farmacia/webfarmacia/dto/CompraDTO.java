package com.proyecto.farmacia.webfarmacia.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CompraDTO {
    public Long proveedorId;
    public LocalDateTime fecha;
    public BigDecimal total;
    public List<DetalleCompraDTO> productos;
} 