package com.proyecto.farmacia.webfarmacia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.proyecto.farmacia.webfarmacia.model.DetalleVenta;
import com.proyecto.farmacia.webfarmacia.service.DetalleVentaService;
import com.proyecto.farmacia.webfarmacia.dto.CategoriaVentaDTO;
import com.proyecto.farmacia.webfarmacia.dto.ProductoVentaDTO;
import com.proyecto.farmacia.webfarmacia.dto.DetalleVentaDTO;
import java.util.List;

@RestController
@RequestMapping("/api/detalle-venta")
public class DetalleVentaController {
    @Autowired
    private DetalleVentaService detalleVentaService;

    @GetMapping
    public List<DetalleVenta> getAllDetalleVentas() {
        return detalleVentaService.getAllDetalleVentas();
    }

    @PostMapping
    public ResponseEntity<DetalleVenta> createDetalleVenta(@RequestBody DetalleVenta detalleVenta) {
        DetalleVenta nuevoDetalleVenta = detalleVentaService.saveDetalleVenta(detalleVenta);
        return ResponseEntity.ok(nuevoDetalleVenta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetalleVenta> getDetalleVentaById(@PathVariable Long id) {
        DetalleVenta detalleVenta = detalleVentaService.getDetalleVentaById(id);
        if (detalleVenta != null) {
            return ResponseEntity.ok(detalleVenta);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDetalleVenta(@PathVariable Long id) {
        detalleVentaService.deleteDetalleVenta(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/total-profit")
    public Double getTotalProfit() {
        return detalleVentaService.calcularTotalProfit();
    }

    @GetMapping("/total-revenue")
    public Double getTotalRevenue() {
        return detalleVentaService.calcularTotalRevenue();
    }

    @GetMapping("/net-sales-value")
    public Double getNetSalesValue() {
        return detalleVentaService.calcularNetSalesValue();
    }

    @GetMapping("/mejores-categorias")
    public List<CategoriaVentaDTO> getMejoresCategoriasVentas() {
        return detalleVentaService.getMejoresCategoriasVentas();
    }

    @GetMapping("/best-selling-products")
    public List<ProductoVentaDTO> getBestSellingProducts() {
        return detalleVentaService.getBestSellingProducts();
    }

    @GetMapping("/venta/{ventaId}")
    public ResponseEntity<List<DetalleVentaDTO>> getDetalleVentaByVentaId(@PathVariable Long ventaId) {
        List<DetalleVentaDTO> detalles = detalleVentaService.getDetalleVentaDTOByVentaId(ventaId);
        if (detalles != null && !detalles.isEmpty()) {
            return ResponseEntity.ok(detalles);
        }
        return ResponseEntity.notFound().build();
    }
} 