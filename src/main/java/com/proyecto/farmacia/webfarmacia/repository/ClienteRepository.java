package com.proyecto.farmacia.webfarmacia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.farmacia.webfarmacia.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
} 