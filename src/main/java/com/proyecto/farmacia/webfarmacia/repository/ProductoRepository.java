package com.proyecto.farmacia.webfarmacia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.farmacia.webfarmacia.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
} 