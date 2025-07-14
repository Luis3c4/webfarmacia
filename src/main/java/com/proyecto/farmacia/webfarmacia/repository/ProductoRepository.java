package com.proyecto.farmacia.webfarmacia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.farmacia.webfarmacia.model.Producto;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
    // Consultas simples sin JPQL complejo
    List<Producto> findByActivoTrue();
    
    Page<Producto> findByActivoTrue(Pageable pageable);
    
    // Consultas de búsqueda simplificadas
    List<Producto> findByActivoTrueAndNombreContainingIgnoreCase(String nombre);
    
    Page<Producto> findByActivoTrueAndNombreContainingIgnoreCase(String nombre, Pageable pageable);
    
    Page<Producto> findByActivoTrueAndCategoriaContainingIgnoreCase(String categoria, Pageable pageable);
    
    @Query("SELECT DISTINCT p.categoria FROM Producto p WHERE p.activo = true AND p.categoria IS NOT NULL AND p.categoria != ''")
    List<String> findDistinctCategoriasByActivoTrue();

    Producto findBySupabaseId(String supabaseId);
    
    // Consultas JPQL originales como respaldo (comentadas por ahora)
    /*
    @Query("SELECT p FROM Producto p WHERE p.activo = true")
    List<Producto> findAllActivos();

    @Query("SELECT p FROM Producto p WHERE p.activo = true")
    Page<Producto> findAllActivosPage(Pageable pageable);
    
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Producto> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);
    
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    Page<Producto> findByNombreContainingIgnoreCasePage(@Param("nombre") String nombre, Pageable pageable);
    
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND LOWER(p.categoria) LIKE LOWER(CONCAT('%', :categoria, '%'))")
    Page<Producto> findByCategoriaContainingIgnoreCaseAndActivoTrue(@Param("categoria") String categoria, Pageable pageable);
    */
} 