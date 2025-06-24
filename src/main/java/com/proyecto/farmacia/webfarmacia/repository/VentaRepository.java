package com.proyecto.farmacia.webfarmacia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.proyecto.farmacia.webfarmacia.model.Venta;

public interface VentaRepository extends JpaRepository<Venta, Long> {
    @Query("SELECT COUNT(v) FROM Venta v")
    Long contarTotalVentas();
} 