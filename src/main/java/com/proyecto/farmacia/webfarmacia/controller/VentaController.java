package com.proyecto.farmacia.webfarmacia.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.farmacia.webfarmacia.model.DetalleVenta;
import com.proyecto.farmacia.webfarmacia.model.Producto;
import com.proyecto.farmacia.webfarmacia.model.Usuario;
import com.proyecto.farmacia.webfarmacia.model.Venta;
import com.proyecto.farmacia.webfarmacia.repository.DetalleVentaRepository;
import com.proyecto.farmacia.webfarmacia.repository.ProductoRepository;
import com.proyecto.farmacia.webfarmacia.repository.UsuarioRepository;
import com.proyecto.farmacia.webfarmacia.service.VentaService;
import com.proyecto.farmacia.webfarmacia.service.DetalleVentaService;
import com.proyecto.farmacia.webfarmacia.dto.VentaDTO;
import com.proyecto.farmacia.webfarmacia.dto.ProductoVentaDTO;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {
    @Autowired
    private VentaService ventaService;
    @Autowired
    private DetalleVentaService detalleVentaService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private DetalleVentaRepository detalleVentaRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping
    public List<VentaDTO> getAllVentas() {
        return ventaService.getAllVentasDTO();
    }

    @PostMapping
    public ResponseEntity<Venta> createVenta(@RequestBody Venta venta) {
        Venta nuevaVenta = ventaService.saveVenta(venta);
        return ResponseEntity.ok(nuevaVenta);
    }

    @PostMapping("/api/checkout")
    public ResponseEntity<?> checkout(@RequestBody Map<String, Object> payload) {
        // Aquí puedes procesar los items, crear la orden, validar stock, etc.
        // Por ahora solo responde con éxito
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/api/ventas/confirmar")
    @Transactional
    public ResponseEntity<?> confirmarVenta(@RequestBody Map<String, Object> payload) {
        try {
            // Obtener datos del payload
            List<Map<String, Object>> items = objectMapper.convertValue(
                payload.get("items"),
                new TypeReference<List<Map<String, Object>>>() {}
            );
            Number total = (Number) payload.get("total");
            Number idUsuario = (Number) payload.get("id_usuario");
            String metodoPago = (String) payload.getOrDefault("metodo_pago", "desconocido");

            if (items == null || items.isEmpty() || idUsuario == null) {
                return ResponseEntity.badRequest().body("Datos incompletos para registrar la venta");
            }

            Usuario usuario = usuarioRepository.findById(idUsuario.longValue()).orElse(null);
            if (usuario == null) {
                return ResponseEntity.badRequest().body("Usuario no encontrado");
            }

            // Crear la venta
            Venta venta = new Venta();
            venta.setUsuario(usuario);
            venta.setFecha(LocalDateTime.now());
            venta.setTotal(BigDecimal.valueOf(total != null ? total.doubleValue() : 0));
            venta.setMetodoPago(metodoPago);
            Venta ventaGuardada = ventaService.saveVenta(venta);

            // Guardar los detalles de venta
            for (Map<String, Object> item : items) {
                Number idProducto = (Number) item.get("id");
                Number cantidad = (Number) item.get("cantidad");
                Number precio = (Number) item.get("precio");
                if (idProducto == null || cantidad == null || precio == null) continue;
                Producto producto = productoRepository.findById(idProducto.longValue()).orElse(null);
                if (producto == null) continue;
                DetalleVenta detalle = new DetalleVenta();
                detalle.setVenta(ventaGuardada);
                detalle.setProducto(producto);
                detalle.setCantidad(cantidad.intValue());
                detalle.setPrecioUnitario(BigDecimal.valueOf(precio.doubleValue()));
                detalleVentaRepository.save(detalle);
            }

            return ResponseEntity.ok(Map.of("status", "ok", "venta_id", ventaGuardada.getVentaId()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al registrar la venta: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaDTO> getVentaById(@PathVariable Long id) {
        VentaDTO venta = ventaService.getVentaDTOById(id);
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

    // Nuevo endpoint optimizado que combina todos los datos del dashboard
    @GetMapping("/dashboard-data")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        try {
            // Obtener todos los datos en paralelo para mejor rendimiento
            Double totalRevenue = detalleVentaService.calcularTotalRevenue();
            Long totalSales = ventaService.contarTotalVentas();
            List<VentaDTO> recentVentas = ventaService.getAllVentasDTO();
            List<ProductoVentaDTO> bestProducts = detalleVentaService.getBestSellingProducts();
            
            // Limitar las ventas recientes a las últimas 5 para mejor rendimiento
            List<VentaDTO> limitedVentas = recentVentas.size() > 5 ? 
                recentVentas.subList(recentVentas.size() - 5, recentVentas.size()) : 
                recentVentas;
            
            Map<String, Object> dashboardData = Map.of(
                "totalRevenue", totalRevenue != null ? totalRevenue : 0.0,
                "totalSales", totalSales != null ? totalSales : 0L,
                "recentVentas", limitedVentas,
                "bestProducts", bestProducts != null ? bestProducts : List.of(),
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error al cargar datos del dashboard: " + e.getMessage()
            ));
        }
    }
} 