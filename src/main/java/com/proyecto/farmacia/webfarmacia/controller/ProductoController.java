package com.proyecto.farmacia.webfarmacia.controller;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.farmacia.webfarmacia.model.Producto;
import com.proyecto.farmacia.webfarmacia.service.ProductoService;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/productos")
@EnableSpringDataWebSupport
public class ProductoController {
    @Autowired
    private ProductoService productoService;

    //crear producto
    @PostMapping
    public ResponseEntity<Producto> createProducto(@RequestBody Producto producto) {
        try {
            System.out.println("=== CREANDO PRODUCTO ===");
            System.out.println("Datos recibidos: " + producto);
            
            // Establecer valores por defecto
            if (producto.getStock() < 0) {
                producto.setStock(0);
            }
            
            // Establecer el estado del stock basado en la cantidad
            if (producto.getStock() <= 0) {
                producto.setStockStatus("OUT_OF_STOCK");
            } else if (producto.getStock() <= 10) {
                producto.setStockStatus("LOW_STOCK");
            } else {
                producto.setStockStatus("IN_STOCK");
            }
            
            // Establecer activo como true por defecto
            producto.setActivo(true);
            
            // Si no hay coste unitario, establecerlo como 0
            if (producto.getCosteUnitario() == null) {
                producto.setCosteUnitario(BigDecimal.ZERO);
            }
            
            Producto nuevoProducto = productoService.saveProducto(producto);
            System.out.println("Producto creado exitosamente con ID: " + nuevoProducto.getProductoId());
            System.out.println("===============================");
            return ResponseEntity.ok(nuevoProducto);
            
        } catch (Exception e) {
            System.err.println("Error al crear producto: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
    //obtener producto por id
    @GetMapping("/{id}")
    public ResponseEntity<Producto> getProductoById(@PathVariable Long id) {
        Producto producto = productoService.getProductoById(id);
        if (producto != null) {
            return ResponseEntity.ok(producto);
        }
        return ResponseEntity.notFound().build();
    }
    //editar producto
    @PutMapping("/{id}")
    public ResponseEntity<Producto> updateProducto(@PathVariable Long id, @RequestBody Producto producto) {
        try {
            System.out.println("=== ACTUALIZANDO PRODUCTO ===");
            System.out.println("ID: " + id);
            System.out.println("Datos recibidos: " + producto);
            
            Producto productoExistente = productoService.getProductoById(id);
            if (productoExistente == null) {
                System.out.println("Producto no encontrado con ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            // Actualizar los campos del producto existente
            if (producto.getNombre() != null && !producto.getNombre().trim().isEmpty()) {
                productoExistente.setNombre(producto.getNombre().trim());
            }
            
            // El frontend envía 'precio', usar el getter temporal
            if (producto.getPrecio() > 0) {
                productoExistente.setPrecio_unitario(BigDecimal.valueOf(producto.getPrecio()));
            }
            
            if (producto.getCantidadIngresada() >= 0) {
                productoExistente.setCantidadIngresada(producto.getCantidadIngresada());
            }
            
            if (producto.getDescripcion() != null) {
                productoExistente.setDescripcion(producto.getDescripcion().trim());
            }
            
            // Actualizar la categoría
            if (producto.getCategoria() != null) {
                String categoria = producto.getCategoria().trim();
                productoExistente.setCategoria(categoria.isEmpty() ? null : categoria);
                System.out.println("Categoría actualizada: " + categoria);
            }
            
            // Actualizar los nuevos campos
            if (producto.getFechaProducto() != null) {
                productoExistente.setFechaProducto(producto.getFechaProducto());
            }
            
            if (producto.getDescuentoDouble() > 0) {
                productoExistente.setDescuento(BigDecimal.valueOf(producto.getDescuentoDouble()));
            }
            
            if (producto.getMetodoEntrega() != null && !producto.getMetodoEntrega().trim().isEmpty()) {
                productoExistente.setMetodoEntrega(producto.getMetodoEntrega().trim());
            }
            
            // Actualizar la imagen solo si se proporciona una nueva
            if (producto.getImgUrl() != null && !producto.getImgUrl().trim().isEmpty()) {
                productoExistente.setImgUrl(producto.getImgUrl().trim());
            }
            
            // Actualizar el stock y el estado del stock
            if (producto.getStock() >= 0) {
                productoExistente.setStock(producto.getStock());
            }
            
            // Actualizar el estado del stock basado en el stock actual
            int stockActual = productoExistente.getStock();
            if (stockActual <= 0) {
                productoExistente.setStockStatus("OUT_OF_STOCK");
            } else if (stockActual <= 10) {
                productoExistente.setStockStatus("LOW_STOCK");
            } else {
                productoExistente.setStockStatus("IN_STOCK");
            }
            
            // También actualizar el stockStatus si viene del frontend y es válido
            if (producto.getStockStatus() != null && !producto.getStockStatus().trim().isEmpty()) {
                String stockStatus = producto.getStockStatus().trim();
                if (stockStatus.equals("IN_STOCK") || 
                    stockStatus.equals("OUT_OF_STOCK") || 
                    stockStatus.equals("LOW_STOCK")) {
                    productoExistente.setStockStatus(stockStatus);
                }
            }

            Producto productoActualizado = productoService.saveProducto(productoExistente);
            System.out.println("Producto actualizado exitosamente");
            System.out.println("===============================");
            return ResponseEntity.ok(productoActualizado);
            
        } catch (Exception e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProducto(@PathVariable Long id) {
        productoService.deleteProducto(id);
        return ResponseEntity.ok().build();
    }
    
    // Agregar stock a un producto
    @PostMapping("/{id}/agregar-stock")
    public ResponseEntity<Producto> agregarStock(@PathVariable Long id, @RequestBody AgregarStockRequest request) {
        try {
            Producto producto = productoService.getProductoById(id);
            if (producto == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Agregar la nueva cantidad ingresada al stock existente
            int nuevaCantidadIngresada = request.getCantidadIngresada();
            int stockActual = producto.getStock();
            int nuevoStock = stockActual + nuevaCantidadIngresada;
            
            // Actualizar la cantidad ingresada total (sumar a la existente)
            int cantidadIngresadaTotal = producto.getCantidadIngresada() + nuevaCantidadIngresada;
            
            producto.setCantidadIngresada(cantidadIngresadaTotal);
            producto.setStock(nuevoStock);
            
            // Actualizar el estado del stock
            if (nuevoStock <= 0) {
                producto.setStockStatus("OUT_OF_STOCK");
            } else if (nuevoStock <= 10) {
                producto.setStockStatus("LOW_STOCK");
            } else {
                producto.setStockStatus("IN_STOCK");
            }
            
            Producto productoActualizado = productoService.saveProducto(producto);
            return ResponseEntity.ok(productoActualizado);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    //obtener productos
    @GetMapping
    public ResponseEntity<Page<Producto>> getProductosPage(@PageableDefault(size = 10) Pageable pageable) {
        try {
            System.out.println("Solicitud de productos recibida - Página: " + pageable.getPageNumber() + ", Tamaño: " + pageable.getPageSize());
            Page<Producto> productos = productoService.getProductosPage(pageable);
            System.out.println("Productos encontrados: " + productos.getTotalElements() + ", Contenido: " + productos.getContent().size());
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            System.err.println("Error al obtener productos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
    
    //buscar productos por nombre
    @GetMapping("/buscar")
    public ResponseEntity<Page<Producto>> getProductosByNombre(@RequestParam String nombre, @PageableDefault(size = 10) Pageable pageable) {
        try {
            return ResponseEntity.ok(productoService.getProductosByNombrePage(nombre, pageable));
        } catch (Exception e) {
            System.err.println("Error al buscar productos por nombre: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
    
    //buscar productos por categoría
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<Page<Producto>> getProductosByCategoria(@PathVariable String categoria, @PageableDefault(size = 10) Pageable pageable) {
        try {
            return ResponseEntity.ok(productoService.getProductosByCategoriaPage(categoria, pageable));
        } catch (Exception e) {
            System.err.println("Error al buscar productos por categoría: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
    
    //obtener todas las categorías disponibles
    @GetMapping("/categorias")
    public ResponseEntity<List<String>> getCategorias() {
        List<String> categorias = productoService.getCategoriasDisponibles();
        return ResponseEntity.ok(categorias);
    }
    
    /**
     * Verifica si hay stock suficiente para un producto
     */
    @GetMapping("/{id}/stock")
    public ResponseEntity<Map<String, Object>> verificarStock(@PathVariable Long id, 
                                                             @RequestParam int cantidad) {
        try {
            Producto producto = productoService.getProductoById(id);
            if (producto == null) {
                return ResponseEntity.notFound().build();
            }
            
            boolean stockDisponible = productoService.verificarStockDisponible(id, cantidad);
            
            Map<String, Object> response = new HashMap<>();
            response.put("productoId", id);
            response.put("nombreProducto", producto.getNombre());
            response.put("stockActual", producto.getStock());
            response.put("cantidadSolicitada", cantidad);
            response.put("stockDisponible", stockDisponible);
            response.put("stockStatus", producto.getStockStatus());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error al verificar stock: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }
    
    /**
     * Actualiza el stock de un producto (solo para administradores)
     */
    @PutMapping("/{id}/stock")
    public ResponseEntity<Map<String, Object>> actualizarStock(@PathVariable Long id, 
                                                              @RequestParam int nuevaCantidad) {
        try {
            Producto producto = productoService.getProductoById(id);
            if (producto == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Actualizar stock
            producto.setStock(nuevaCantidad);
            
            // Actualizar estado del stock automáticamente
            if (nuevaCantidad <= 0) {
                producto.setStockStatus("OUT_OF_STOCK");
            } else if (nuevaCantidad <= 10) {
                producto.setStockStatus("LOW_STOCK");
            } else {
                producto.setStockStatus("IN_STOCK");
            }
            
            producto.setUpdatedAt(java.time.LocalDateTime.now());
            Producto productoActualizado = productoService.saveProducto(producto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("productoId", id);
            response.put("nombreProducto", productoActualizado.getNombre());
            response.put("stockActual", productoActualizado.getStock());
            response.put("stockStatus", productoActualizado.getStockStatus());
            response.put("mensaje", "Stock actualizado exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error al actualizar stock: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }
    
    // Endpoint temporal para agregar datos de prueba
    @PostMapping("/init-data")
    public ResponseEntity<String> initializeTestData() {
        try {
            // Verificar si ya hay productos
            Page<Producto> existingProducts = productoService.getProductosPage(org.springframework.data.domain.PageRequest.of(0, 1));
            
            if (existingProducts.getTotalElements() > 0) {
                return ResponseEntity.ok("Ya existen productos en la base de datos. Total: " + existingProducts.getTotalElements());
            }
            
            // Crear productos de prueba con URLs de Supabase Storage
            Producto[] productosPrueba = {
                createProducto("Paracetamol 500mg", "Analgésico y antipirético para el alivio del dolor y la fiebre", 15.90, 100, 85, "https://via.placeholder.com/300x300/3498db/ffffff?text=Paracetamol", "Analgésicos"),
                createProducto("Ibuprofeno 400mg", "Antiinflamatorio no esteroideo para dolor e inflamación", 18.50, 75, 12, "https://via.placeholder.com/300x300/27ae60/ffffff?text=Ibuprofeno", "Antiinflamatorios"),
                createProducto("Vitamina C 1000mg", "Suplemento vitamínico para fortalecer el sistema inmune", 25.99, 50, 0, "https://via.placeholder.com/300x300/e74c3c/ffffff?text=Vitamina+C", "Vitaminas"),
                createProducto("Omeprazol 20mg", "Protector gástrico para el tratamiento de la acidez", 32.75, 60, 45, "https://via.placeholder.com/300x300/f39c12/ffffff?text=Omeprazol", "Gastritis"),
                createProducto("Aspirina 100mg", "Anticoagulante y analgésico para prevención cardiovascular", 12.25, 80, 8, "https://via.placeholder.com/300x300/9b59b6/ffffff?text=Aspirina", "Analgésicos"),
                createProducto("Diclofenaco 50mg", "Antiinflamatorio para el tratamiento del dolor articular", 22.99, 40, 25, "https://via.placeholder.com/300x300/34495e/ffffff?text=Diclofenaco", "Antiinflamatorios"),
                createProducto("Jabón Antibacterial", "Jabón para higiene personal con acción antibacterial", 8.50, 120, 95, "https://via.placeholder.com/300x300/1abc9c/ffffff?text=Jabon", "Higiene"),
                createProducto("Shampoo Anticaspa", "Shampoo especializado para el control de la caspa", 15.75, 80, 60, "https://via.placeholder.com/300x300/2ecc71/ffffff?text=Shampoo", "Higiene"),
                createProducto("Crema Hidratante", "Crema facial hidratante para todo tipo de piel", 28.99, 60, 45, "https://via.placeholder.com/300x300/e67e22/ffffff?text=Crema", "Dermocosmética"),
                createProducto("Protector Solar SPF 50", "Protector solar de amplio espectro para piel sensible", 35.50, 40, 30, "https://via.placeholder.com/300x300/f1c40f/ffffff?text=Protector", "Dermocosmética"),
                createProducto("Jarabe para la Tos", "Jarabe natural para aliviar la tos seca y húmeda", 18.25, 70, 55, "https://via.placeholder.com/300x300/95a5a6/ffffff?text=Jarabe", "Niños"),
                createProducto("Vitamina D3", "Suplemento de vitamina D para fortalecer huesos", 22.99, 45, 35, "https://via.placeholder.com/300x300/8e44ad/ffffff?text=Vitamina+D", "Vitaminas")
            };
            
            for (Producto producto : productosPrueba) {
                productoService.saveProducto(producto);
            }
            
            return ResponseEntity.ok("Datos de prueba agregados exitosamente. Se crearon " + productosPrueba.length + " productos.");
            
        } catch (Exception e) {
            System.err.println("Error al inicializar datos de prueba: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al inicializar datos: " + e.getMessage());
        }
    }
    
    private Producto createProducto(String nombre, String descripcion, double precio, int cantidadIngresada, int stock, String imgUrl, String categoria) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio_unitario(BigDecimal.valueOf(precio));
        producto.setCantidadIngresada(cantidadIngresada);
        producto.setStock(stock);
        producto.setImgUrl(imgUrl);
        producto.setCategoria(categoria);
        
        // Agregar datos de ejemplo para los nuevos campos
        producto.setFechaProducto(java.time.LocalDate.now());
        producto.setDescuento(new java.math.BigDecimal("0.00"));
        producto.setMetodoEntrega("delivery");
        
        // Determinar el estado del stock
        if (stock <= 0) {
            producto.setStockStatus("OUT_OF_STOCK");
        } else if (stock <= 10) {
            producto.setStockStatus("LOW_STOCK");
        } else {
            producto.setStockStatus("IN_STOCK");
        }
        
        return producto;
    }
    
    // Clase interna para la solicitud de agregar stock
    public static class AgregarStockRequest {
        private int cantidadIngresada;
        
        public AgregarStockRequest() {}
        
        public AgregarStockRequest(int cantidadIngresada) {
            this.cantidadIngresada = cantidadIngresada;
        }
        
        public int getCantidadIngresada() {
            return cantidadIngresada;
        }
        
        public void setCantidadIngresada(int cantidadIngresada) {
            this.cantidadIngresada = cantidadIngresada;
        }
    }

    @GetMapping("/test-images")
    public ResponseEntity<Map<String, Object>> testImages() {
        try {
            Map<String, Object> response = new HashMap<>();
            List<Producto> productos = productoService.getAllProductos();
            
            List<Map<String, Object>> productosInfo = new ArrayList<>();
            for (Producto producto : productos) {
                Map<String, Object> info = new HashMap<>();
                info.put("id", producto.getProductoId());
                info.put("nombre", producto.getNombre());
                info.put("imgUrl", producto.getImgUrl());
                info.put("tieneImagen", producto.getImgUrl() != null && !producto.getImgUrl().trim().isEmpty());
                productosInfo.add(info);
            }
            
            response.put("totalProductos", productos.size());
            response.put("productosConImagen", productos.stream()
                .filter(p -> p.getImgUrl() != null && !p.getImgUrl().trim().isEmpty())
                .count());
            response.put("productosSinImagen", productos.stream()
                .filter(p -> p.getImgUrl() == null || p.getImgUrl().trim().isEmpty())
                .count());
            response.put("productos", productosInfo);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
