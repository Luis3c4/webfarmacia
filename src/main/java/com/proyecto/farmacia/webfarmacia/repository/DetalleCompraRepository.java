package com.proyecto.farmacia.webfarmacia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.proyecto.farmacia.webfarmacia.model.DetalleCompra;
 
public interface DetalleCompraRepository extends JpaRepository<DetalleCompra, Long> {
    @Query("SELECT SUM(dc.costeUnitario * dc.cantidad) - SUM(dc.descuentos) FROM DetalleCompra dc")
    Double calcularNetPurchaseValue();
} 