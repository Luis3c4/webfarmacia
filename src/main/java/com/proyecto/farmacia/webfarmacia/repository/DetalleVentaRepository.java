package com.proyecto.farmacia.webfarmacia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.proyecto.farmacia.webfarmacia.model.DetalleVenta;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
    @Query("SELECT SUM((dv.precioUnitario - dv.costeUnitario) * dv.cantidad) - SUM(dv.descuentos) " +
           "FROM DetalleVenta dv JOIN dv.venta v")
    Double calcularTotalProfit();

    @Query("SELECT SUM(dv.precioUnitario * dv.cantidad) FROM DetalleVenta dv")
    Double calcularTotalRevenue();
} 