package com.proyecto.farmacia.webfarmacia.repository;

import com.proyecto.farmacia.webfarmacia.model.DetalleCompra;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
 
@Repository
public interface DetalleCompraRepository extends JpaRepository<DetalleCompra, Long> {
    @Query("SELECT d FROM DetalleCompra d ORDER BY d.detalleId DESC")
    List<DetalleCompra> findTopNByOrderByDetalleIdDesc(Pageable pageable);

    default List<DetalleCompra> findTopNByOrderByDetalleIdDesc(int limit) {
        return findTopNByOrderByDetalleIdDesc(org.springframework.data.domain.PageRequest.of(0, limit));
    }

    @Query("SELECT SUM(d.cantidad * d.costeUnitario - d.descuentos) FROM DetalleCompra d")
    Double sumTotalCompra();
} 