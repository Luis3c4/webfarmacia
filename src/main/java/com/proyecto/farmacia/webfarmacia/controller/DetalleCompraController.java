package com.proyecto.farmacia.webfarmacia.controller;

import com.proyecto.farmacia.webfarmacia.model.DetalleCompra;
import com.proyecto.farmacia.webfarmacia.repository.DetalleCompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/detalle-compra")
public class DetalleCompraController {
    @Autowired
    private DetalleCompraRepository detalleCompraRepository;

    @GetMapping("/recientes")
    public List<DetalleCompra> getDetallesRecientes(@RequestParam(defaultValue = "20") int limit) {
        return detalleCompraRepository.findTopNByOrderByDetalleIdDesc(limit);
    }
} 