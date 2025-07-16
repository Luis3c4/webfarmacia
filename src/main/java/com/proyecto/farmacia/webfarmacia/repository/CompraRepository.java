package com.proyecto.farmacia.webfarmacia.repository;

import com.proyecto.farmacia.webfarmacia.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
} 