package com.proyecto.farmacia.webfarmacia.service;

import com.proyecto.farmacia.webfarmacia.dto.CompraDTO;
import com.proyecto.farmacia.webfarmacia.dto.DetalleCompraDTO;
import com.proyecto.farmacia.webfarmacia.model.Compra;
import com.proyecto.farmacia.webfarmacia.model.Producto;
import com.proyecto.farmacia.webfarmacia.model.DetalleCompra;
import com.proyecto.farmacia.webfarmacia.repository.CompraRepository;
import com.proyecto.farmacia.webfarmacia.repository.ProductoRepository;
import com.proyecto.farmacia.webfarmacia.repository.DetalleCompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CompraService {
    @Autowired
    private CompraRepository compraRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private DetalleCompraRepository detalleCompraRepository;

    public List<Compra> getAllCompras() {
        return compraRepository.findAll();
    }

    public Optional<Compra> getCompraById(Long id) {
        return compraRepository.findById(id);
    }

    public BigDecimal getTotalRevenue() {
        List<Compra> compras = compraRepository.findAll();
        return compras.stream()
                .map(Compra::getTotal)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getTotalCount() {
        return compraRepository.count();
    }

    public BigDecimal getAveragePurchase() {
        List<Compra> compras = compraRepository.findAll();
        if (compras.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalRevenue = getTotalRevenue();
        return totalRevenue.divide(BigDecimal.valueOf(compras.size()), 2, BigDecimal.ROUND_HALF_UP);
    }

    public Map<String, Object> getMonthlyStats() {
        List<Compra> compras = compraRepository.findAll();
        Map<String, Object> stats = new HashMap<>();
        
        // Estadísticas básicas
        stats.put("totalCompras", compras.size());
        stats.put("totalRevenue", getTotalRevenue());
        stats.put("averagePurchase", getAveragePurchase());
        
        // Compras del mes actual
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        long comprasEsteMes = compras.stream()
                .filter(compra -> compra.getFecha() != null && compra.getFecha().isAfter(startOfMonth))
                .count();
        
        BigDecimal revenueEsteMes = compras.stream()
                .filter(compra -> compra.getFecha() != null && compra.getFecha().isAfter(startOfMonth))
                .map(Compra::getTotal)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        stats.put("comprasEsteMes", comprasEsteMes);
        stats.put("revenueEsteMes", revenueEsteMes);
        
        return stats;
    }

    public List<Compra> getRecentCompras(int limit) {
        List<Compra> allCompras = compraRepository.findAll();
        return allCompras.stream()
                .sorted((c1, c2) -> {
                    if (c1.getFecha() == null && c2.getFecha() == null) return 0;
                    if (c1.getFecha() == null) return 1;
                    if (c2.getFecha() == null) return -1;
                    return c2.getFecha().compareTo(c1.getFecha());
                })
                .limit(limit)
                .toList();
    }

    public List<Compra> getComprasByProvider(Long providerId) {
        List<Compra> allCompras = compraRepository.findAll();
        return allCompras.stream()
                .filter(compra -> providerId.equals(compra.getProveedorId()))
                .toList();
    }

    @Transactional
    public Compra registrarCompra(CompraDTO compraDTO) {
        Compra compra = new Compra();
        compra.setProveedorId(compraDTO.proveedorId);
        compra.setFecha(compraDTO.fecha);
        compra.setTotal(compraDTO.total);
        compra = compraRepository.save(compra);

        for (DetalleCompraDTO d : compraDTO.productos) {
            DetalleCompra detalle = new DetalleCompra();
            detalle.setCompra(compra);
            detalle.setCantidad(d.cantidad);
            detalle.setCosteUnitario(d.costeUnitario);
            detalle.setDescuentos(d.descuentos);

            Producto producto = productoRepository.findById(d.productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + d.productoId));
            detalle.setProducto(producto);

            // Aumentar el stock
            producto.setStock(producto.getStock() + d.cantidad);
            productoRepository.save(producto);

            detalleCompraRepository.save(detalle);
        }
        return compra;
    }
} 