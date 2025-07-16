package com.proyecto.farmacia.webfarmacia.controller;

import com.proyecto.farmacia.webfarmacia.model.Compra;
import com.proyecto.farmacia.webfarmacia.service.CompraService;
import com.proyecto.farmacia.webfarmacia.dto.CompraDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/compras")
@CrossOrigin(origins = "*")
public class CompraController {
    @Autowired
    private CompraService compraService;

    @GetMapping
    public List<Compra> getAllCompras() {
        return compraService.getAllCompras();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Compra> getCompraById(@PathVariable Long id) {
        Optional<Compra> compra = compraService.getCompraById(id);
        return compra.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats/total-revenue")
    public ResponseEntity<BigDecimal> getTotalRevenue() {
        BigDecimal totalRevenue = compraService.getTotalRevenue();
        return ResponseEntity.ok(totalRevenue);
    }

    @GetMapping("/stats/total-count")
    public ResponseEntity<Long> getTotalCount() {
        Long totalCount = compraService.getTotalCount();
        return ResponseEntity.ok(totalCount);
    }

    @GetMapping("/stats/average-purchase")
    public ResponseEntity<BigDecimal> getAveragePurchase() {
        BigDecimal averagePurchase = compraService.getAveragePurchase();
        return ResponseEntity.ok(averagePurchase);
    }

    @GetMapping("/stats/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyStats() {
        Map<String, Object> monthlyStats = compraService.getMonthlyStats();
        return ResponseEntity.ok(monthlyStats);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Compra>> getRecentCompras(@RequestParam(defaultValue = "10") int limit) {
        List<Compra> recentCompras = compraService.getRecentCompras(limit);
        return ResponseEntity.ok(recentCompras);
    }

    @GetMapping("/by-provider/{providerId}")
    public ResponseEntity<List<Compra>> getComprasByProvider(@PathVariable Long providerId) {
        List<Compra> compras = compraService.getComprasByProvider(providerId);
        return ResponseEntity.ok(compras);
    }

    @PostMapping
    public ResponseEntity<Compra> registrarCompra(@RequestBody CompraDTO compraDTO) {
        Compra compra = compraService.registrarCompra(compraDTO);
        return ResponseEntity.ok(compra);
    }
} 