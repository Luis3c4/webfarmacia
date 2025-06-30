package com.proyecto.farmacia.webfarmacia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.farmacia.webfarmacia.model.Producto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    @Query("SELECT p FROM Producto p WHERE p.activo = true")
    List<Producto> findAllActivos();

    @Query("SELECT p FROM Producto p WHERE p.activo = true")
    Page<Producto> findAllActivosPage(Pageable pageable);
    
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Producto> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);
    
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    Page<Producto> findByNombreContainingIgnoreCasePage(@Param("nombre") String nombre, Pageable pageable);
} 