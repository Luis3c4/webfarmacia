package com.proyecto.farmacia.webfarmacia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.farmacia.webfarmacia.model.Producto;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    @Query("SELECT p FROM Producto p WHERE p.activo = true")
    List<Producto> findAllActivos();

    @Query("SELECT p FROM Producto p WHERE p.activo = true")
    Page<Producto> findAllActivosPage(Pageable pageable);
} 