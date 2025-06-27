package com.proyecto.farmacia.webfarmacia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.proyecto.farmacia.webfarmacia.model.Venta;
import com.proyecto.farmacia.webfarmacia.service.VentaService;
import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {
    @Autowired
    private VentaService ventaService;

    @GetMapping
    public List<Venta> getAllVentas() {
        return ventaService.getAllVentas();
    }

    @PostMapping
    public ResponseEntity<Venta> createVenta(@RequestBody Venta venta) {
        Venta nuevaVenta = ventaService.saveVenta(venta);
        return ResponseEntity.ok(nuevaVenta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venta> getVentaById(@PathVariable Long id) {
        Venta venta = ventaService.getVentaById(id);
        if (venta != null) {
            return ResponseEntity.ok(venta);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVenta(@PathVariable Long id) {
        ventaService.deleteVenta(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/total-sales")
    public Long getTotalSales() {
        return ventaService.contarTotalVentas();
    }
} 