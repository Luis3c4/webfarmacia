package com.proyecto.farmacia.webfarmacia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.farmacia.webfarmacia.model.Cliente;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    @Query("SELECT c FROM Cliente c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Cliente> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);
    
    @Query("SELECT c FROM Cliente c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    Page<Cliente> findByNombreContainingIgnoreCasePage(@Param("nombre") String nombre, Pageable pageable);
} 