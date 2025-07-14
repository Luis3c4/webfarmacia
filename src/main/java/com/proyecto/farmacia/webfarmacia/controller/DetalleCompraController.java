package com.proyecto.farmacia.webfarmacia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.proyecto.farmacia.webfarmacia.model.DetalleCompra;
import com.proyecto.farmacia.webfarmacia.service.DetalleCompraService;
import java.util.List;

@RestController
@RequestMapping("/api/detalle-compra")
public class DetalleCompraController {
    @Autowired
    private DetalleCompraService detalleCompraService;

    @GetMapping
    public List<DetalleCompra> getAllDetalleCompras() {
        return detalleCompraService.getAllDetalleCompras();
    }

    @PostMapping
    public ResponseEntity<DetalleCompra> createDetalleCompra(@RequestBody DetalleCompra detalleCompra) {
        DetalleCompra nuevoDetalleCompra = detalleCompraService.saveDetalleCompra(detalleCompra);
        return ResponseEntity.ok(nuevoDetalleCompra);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetalleCompra> getDetalleCompraById(@PathVariable Long id) {
        DetalleCompra detalleCompra = detalleCompraService.getDetalleCompraById(id);
        if (detalleCompra != null) {
            return ResponseEntity.ok(detalleCompra);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDetalleCompra(@PathVariable Long id) {
        detalleCompraService.deleteDetalleCompra(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/net-purchase-value")
    public Double getNetPurchaseValue() {
        return detalleCompraService.calcularNetPurchaseValue();
    }
} 