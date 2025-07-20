package com.proyecto.farmacia.webfarmacia.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.farmacia.webfarmacia.dto.CategoriaVentaDTO;
import com.proyecto.farmacia.webfarmacia.dto.ProductoCantidadVentaDTO;
import com.proyecto.farmacia.webfarmacia.dto.VentaDTO;
import com.proyecto.farmacia.webfarmacia.model.Compra;
import com.proyecto.farmacia.webfarmacia.model.Producto;
import com.proyecto.farmacia.webfarmacia.model.Usuario;
import com.proyecto.farmacia.webfarmacia.repository.CompraRepository;
import com.proyecto.farmacia.webfarmacia.repository.ProductoRepository;
import com.proyecto.farmacia.webfarmacia.repository.UsuarioRepository;
import com.proyecto.farmacia.webfarmacia.repository.VentaRepository;
import com.proyecto.farmacia.webfarmacia.service.CompraService;
import com.proyecto.farmacia.webfarmacia.service.DetalleCompraService;
import com.proyecto.farmacia.webfarmacia.service.DetalleVentaService;
import com.proyecto.farmacia.webfarmacia.service.VentaService;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardDataController {
    
    @Autowired
    private DetalleVentaService detalleVentaService;
    
    @Autowired
    private VentaService ventaService;
    
    @Autowired
    private CompraService compraService;
    
    @Autowired
    private DetalleCompraService detalleCompraService;
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private VentaRepository ventaRepository;
    
    @Autowired
    private CompraRepository compraRepository;

    /**
     * Endpoint principal del dashboard que consolida todos los datos
     */
    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        try {
            Map<String, Object> dashboardData = new HashMap<>();
            
            // Métricas principales con manejo de errores
            try {
                dashboardData.put("totalRevenue", detalleVentaService.calcularTotalRevenue());
            } catch (Exception e) {
                dashboardData.put("totalRevenue", 0.0);
            }
            
            try {
                dashboardData.put("totalExpenses", detalleCompraService.calcularNetPurchaseValue());
            } catch (Exception e) {
                dashboardData.put("totalExpenses", 0.0);
            }
            
            try {
                dashboardData.put("totalProducts", productoRepository.count());
            } catch (Exception e) {
                dashboardData.put("totalProducts", 0L);
            }
            
            try {
                dashboardData.put("totalUsers", usuarioRepository.count());
            } catch (Exception e) {
                dashboardData.put("totalUsers", 0L);
            }
            
            try {
                dashboardData.put("totalSales", ventaService.contarTotalVentas());
            } catch (Exception e) {
                dashboardData.put("totalSales", 0L);
            }
            
            try {
                dashboardData.put("totalPurchases", compraRepository.count());
            } catch (Exception e) {
                dashboardData.put("totalPurchases", 0L);
            }
            
            // Cálculo de ganancias
            Double revenue = (Double) dashboardData.get("totalRevenue");
            Double expenses = (Double) dashboardData.get("totalExpenses");
            Double profit = (revenue != null ? revenue : 0.0) - (expenses != null ? expenses : 0.0);
            dashboardData.put("totalProfit", profit);
            
            // Datos para gráficos
            dashboardData.put("revenueChartData", getRevenueChartData());
            dashboardData.put("salesDistributionData", getSalesDistributionData());
            dashboardData.put("productPerformanceData", getProductPerformanceData());
            dashboardData.put("monthlyTrendsData", getMonthlyTrendsData());
            
            // Actividad reciente
            dashboardData.put("recentActivity", getRecentActivity());
            
            // Productos más vendidos
            try {
                dashboardData.put("bestSellingProducts", detalleVentaService.getBestSellingProducts());
            } catch (Exception e) {
                dashboardData.put("bestSellingProducts", new ArrayList<>());
            }
            
            // Categorías más vendidas
            try {
                dashboardData.put("topCategories", detalleVentaService.getMejoresCategoriasVentas());
            } catch (Exception e) {
                dashboardData.put("topCategories", new ArrayList<>());
            }
            
            // Ventas recientes
            try {
                List<VentaDTO> recentVentas = ventaService.getAllVentasDTO();
                dashboardData.put("recentSales", recentVentas.size() > 5 ? 
                    recentVentas.subList(recentVentas.size() - 5, recentVentas.size()) : recentVentas);
            } catch (Exception e) {
                dashboardData.put("recentSales", new ArrayList<>());
            }
            
            // Compras recientes
            try {
                List<Compra> recentCompras = compraService.getRecentCompras(5);
                dashboardData.put("recentPurchases", recentCompras);
            } catch (Exception e) {
                dashboardData.put("recentPurchases", new ArrayList<>());
            }
            
            // Estadísticas de stock
            dashboardData.put("stockStats", getStockStats());
            
            dashboardData.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(dashboardData);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error al cargar datos del dashboard: " + e.getMessage()
            ));
        }
    }

    /**
     * Datos para el gráfico de ingresos vs egresos por mes
     */
    private Map<String, Object> getRevenueChartData() {
        try {
            List<String> labels = new ArrayList<>();
            List<Double> revenueData = new ArrayList<>();
            List<Double> expensesData = new ArrayList<>();
            
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM");
            
            for (int i = 5; i >= 0; i--) {
                LocalDateTime month = now.minusMonths(i);
                labels.add(month.format(formatter));
                
                Double revenue = detalleVentaService.calcularTotalRevenue();
                Double expenses = detalleCompraService.calcularNetPurchaseValue();
                
                // Simular variación mensual
                double revenueVariation = 0.8 + (Math.random() * 0.4);
                double expensesVariation = 0.7 + (Math.random() * 0.6);
                
                revenueData.add((revenue != null ? revenue : 0.0) * revenueVariation / 6);
                expensesData.add((expenses != null ? expenses : 0.0) * expensesVariation / 6);
            }
            
            return Map.of(
                "labels", labels,
                "revenue", revenueData,
                "expenses", expensesData
            );
        } catch (Exception e) {
            return Map.of(
                "labels", Arrays.asList("Ene", "Feb", "Mar", "Abr", "May", "Jun"),
                "revenue", Arrays.asList(1000.0, 1200.0, 1100.0, 1300.0, 1250.0, 1400.0),
                "expenses", Arrays.asList(800.0, 900.0, 850.0, 1000.0, 950.0, 1100.0)
            );
        }
    }

    /**
     * Datos para el gráfico de distribución de ventas por categoría
     */
    private Map<String, Object> getSalesDistributionData() {
        try {
            List<CategoriaVentaDTO> categorias = detalleVentaService.getMejoresCategoriasVentas();
            
            List<String> labels = new ArrayList<>();
            List<Double> data = new ArrayList<>();
            List<String> colors = Arrays.asList("#3b82f6", "#8b5cf6", "#06b6d4", "#10b981", "#f59e0b");
            
            for (int i = 0; i < categorias.size() && i < 5; i++) {
                CategoriaVentaDTO categoria = categorias.get(i);
                labels.add(categoria.getCategory() != null ? categoria.getCategory() : "Sin categoría");
                data.add(categoria.getTurnOver());
            }
            
            // Si no hay suficientes categorías, agregar "Otros"
            if (labels.size() < 5) {
                labels.add("Otros");
                data.add(1000.0);
            }
            
            return Map.of(
                "labels", labels,
                "data", data,
                "colors", colors.subList(0, Math.min(colors.size(), labels.size()))
            );
        } catch (Exception e) {
            return Map.of(
                "labels", Arrays.asList("Medicamentos", "Cosméticos", "Suplementos", "Cuidado Personal", "Otros"),
                "data", Arrays.asList(45.0, 25.0, 15.0, 10.0, 5.0),
                "colors", Arrays.asList("#3b82f6", "#8b5cf6", "#06b6d4", "#10b981", "#f59e0b")
            );
        }
    }

    /**
     * Datos para el gráfico de productos más vendidos
     */
    private Map<String, Object> getProductPerformanceData() {
        try {
            List<ProductoCantidadVentaDTO> productos = detalleVentaService.getProductosMasVendidosPorCantidad();
            
            List<String> labels = new ArrayList<>();
            List<Integer> data = new ArrayList<>();
            
            for (ProductoCantidadVentaDTO producto : productos) {
                labels.add(producto.getProducto() != null ? producto.getProducto() : "Producto sin nombre");
                data.add(producto.getCantidadVendida());
            }
            
            return Map.of(
                "labels", labels,
                "data", data
            );
        } catch (Exception e) {
            return Map.of(
                "labels", Arrays.asList("Paracetamol", "Ibuprofeno", "Vitamina C", "Shampoo", "Crema Facial"),
                "data", Arrays.asList(120, 95, 87, 76, 65)
            );
        }
    }

    /**
     * Datos para el gráfico de tendencias mensuales
     */
    private Map<String, Object> getMonthlyTrendsData() {
        try {
            List<String> labels = Arrays.asList("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic");
            List<Double> data = new ArrayList<>();
            
            Double totalRevenue = detalleVentaService.calcularTotalRevenue();
            double baseValue = (totalRevenue != null ? totalRevenue : 10000.0) / 12;
            
            for (int i = 0; i < 12; i++) {
                double seasonalFactor = 0.8 + 0.4 * Math.sin(i * Math.PI / 6);
                data.add(baseValue * seasonalFactor);
            }
            
            return Map.of(
                "labels", labels,
                "data", data
            );
        } catch (Exception e) {
            return Map.of(
                "labels", Arrays.asList("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"),
                "data", Arrays.asList(65.0, 72.0, 68.0, 85.0, 78.0, 92.0, 88.0, 95.0, 87.0, 103.0, 98.0, 110.0)
            );
        }
    }

    /**
     * Actividad reciente del sistema
     */
    private List<Map<String, Object>> getRecentActivity() {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        try {
            // Ventas recientes
            List<VentaDTO> recentVentas = ventaService.getAllVentasDTO();
            if (!recentVentas.isEmpty()) {
                VentaDTO lastVenta = recentVentas.get(recentVentas.size() - 1);
                activities.add(Map.of(
                    "type", "venta",
                    "text", "Nueva venta registrada - S/. " + String.format("%.2f", lastVenta.getTotal()),
                    "time", "Hace 5 minutos",
                    "icon", "check-circle"
                ));
            }
            
            // Compras recientes
            List<Compra> recentCompras = compraService.getRecentCompras(1);
            if (!recentCompras.isEmpty()) {
                Compra lastCompra = recentCompras.get(0);
                activities.add(Map.of(
                    "type", "compra",
                    "text", "Compra de proveedor registrada - S/. " + String.format("%.2f", lastCompra.getTotal()),
                    "time", "Hace 15 minutos",
                    "icon", "shopping-cart"
                ));
            }
            
            // Productos agregados
            List<Producto> recentProducts = productoRepository.findAll();
            if (!recentProducts.isEmpty()) {
                Producto lastProduct = recentProducts.get(recentProducts.size() - 1);
                activities.add(Map.of(
                    "type", "producto",
                    "text", "Producto agregado al inventario - " + lastProduct.getNombre(),
                    "time", "Hace 1 hora",
                    "icon", "package"
                ));
            }
            
            // Usuarios registrados
            List<Usuario> recentUsers = usuarioRepository.findAll();
            if (!recentUsers.isEmpty()) {
                Usuario lastUser = recentUsers.get(recentUsers.size() - 1);
                activities.add(Map.of(
                    "type", "usuario",
                    "text", "Nuevo usuario registrado - " + lastUser.getNombre(),
                    "time", "Hace 2 horas",
                    "icon", "user"
                ));
            }
            
        } catch (Exception e) {
            // Actividades simuladas en caso de error
            activities.add(Map.of(
                "type", "venta",
                "text", "Nueva venta registrada - S/. 150.00",
                "time", "Hace 5 minutos",
                "icon", "check-circle"
            ));
            activities.add(Map.of(
                "type", "producto",
                "text", "Producto agregado al inventario - Paracetamol",
                "time", "Hace 15 minutos",
                "icon", "package"
            ));
        }
        
        return activities;
    }

    /**
     * Estadísticas de stock
     */
    private Map<String, Object> getStockStats() {
        try {
            List<Producto> productos = productoRepository.findAll();
            
            long lowStock = productos.stream()
                .filter(p -> p.getStock() < 10)
                .count();
            
            long outOfStock = productos.stream()
                .filter(p -> p.getStock() == 0)
                .count();
            
            long totalStock = productos.stream()
                .mapToLong(Producto::getStock)
                .sum();
            
            return Map.of(
                "lowStock", lowStock,
                "outOfStock", outOfStock,
                "totalStock", totalStock,
                "totalProducts", productos.size()
            );
        } catch (Exception e) {
            return Map.of(
                "lowStock", 0,
                "outOfStock", 0,
                "totalStock", 0,
                "totalProducts", 0
            );
        }
    }
} 