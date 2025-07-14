package com.proyecto.farmacia.webfarmacia.controller;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDateTime;
import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

import com.proyecto.farmacia.webfarmacia.service.VentaProcesamientoService;
import com.proyecto.farmacia.webfarmacia.model.Venta;
import com.proyecto.farmacia.webfarmacia.model.Usuario;
import com.proyecto.farmacia.webfarmacia.repository.UsuarioRepository;

@Controller
public class VentaProcesamientoController {

    @Autowired
    private VentaProcesamientoService ventaProcesamientoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private com.proyecto.farmacia.webfarmacia.service.ProductoService productoService;

    @Autowired
    private com.proyecto.farmacia.webfarmacia.service.VentaService ventaService;

    /**
     * Endpoint de debug para verificar datos en la base de datos
     */
    @GetMapping("/debug/check-data")
    @ResponseBody
    public String checkData() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== VERIFICACIÓN DE DATOS ===\n");
            
            // Verificar usuarios
            result.append("\n--- USUARIOS ---\n");
            List<Usuario> usuarios = usuarioRepository.findAll();
            if (usuarios.isEmpty()) {
                result.append("No hay usuarios en la base de datos\n");
            } else {
                for (Usuario usuario : usuarios) {
                    result.append("ID: ").append(usuario.getIdUsuario())
                          .append(", Nombre: ").append(usuario.getNombre())
                          .append(", Email: ").append(usuario.getEmail())
                          .append(", Activo: ").append(usuario.getActivo())
                          .append("\n");
                }
            }
            
            return result.toString();
            
        } catch (Exception e) {
            return "Error al verificar datos: " + e.getMessage() + "\n" + e.getStackTrace()[0];
        }
    }

    /**
     * Endpoint de debug para verificar conectividad y tablas de la base de datos
     */
    @GetMapping("/debug/database-status")
    @ResponseBody
    public String checkDatabaseStatus() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== VERIFICACIÓN DE BASE DE DATOS ===\n");
            
            // Verificar conexión a la base de datos
            result.append("\n--- CONECTIVIDAD ---\n");
            try {
                List<Usuario> usuarios = usuarioRepository.findAll();
                result.append("✅ Conexión a la base de datos exitosa\n");
                result.append("Total de usuarios: ").append(usuarios.size()).append("\n");
            } catch (Exception e) {
                result.append("❌ Error de conexión: ").append(e.getMessage()).append("\n");
                return result.toString();
            }
            
            // Verificar tablas principales
            result.append("\n--- TABLAS PRINCIPALES ---\n");
            
            // Verificar tabla usuarios
            try {
                List<Usuario> usuarios = usuarioRepository.findAll();
                result.append("✅ Tabla usuarios: OK (").append(usuarios.size()).append(" registros)\n");
            } catch (Exception e) {
                result.append("❌ Tabla usuarios: ERROR - ").append(e.getMessage()).append("\n");
            }
            
            // Verificar tabla productos
            try {
                List<com.proyecto.farmacia.webfarmacia.model.Producto> productos = productoService.getAllProductos();
                result.append("✅ Tabla productos: OK (").append(productos.size()).append(" registros)\n");
            } catch (Exception e) {
                result.append("❌ Tabla productos: ERROR - ").append(e.getMessage()).append("\n");
            }
            
            // Verificar tabla ventas
            try {
                List<com.proyecto.farmacia.webfarmacia.model.Venta> ventas = ventaService.getAllVentas();
                result.append("✅ Tabla ventas: OK (").append(ventas.size()).append(" registros)\n");
            } catch (Exception e) {
                result.append("❌ Tabla ventas: ERROR - ").append(e.getMessage()).append("\n");
            }
            
            return result.toString();
            
        } catch (Exception e) {
            return "Error al verificar base de datos: " + e.getMessage() + "\n" + e.getStackTrace()[0];
        }
    }

    /**
     * Endpoint de debug para probar el procesamiento de ventas
     */
    @GetMapping("/debug/venta")
    @ResponseBody
    public String debugVenta() {
        try {
            // Crear un usuario de prueba si no existe
            Usuario testUser = usuarioRepository.findById(1L).orElse(null);
            if (testUser == null) {
                testUser = new Usuario();
                testUser.setIdUsuario(1L);
                testUser.setNombre("Usuario Test");
                testUser.setEmail("test@farmacia.com");
                testUser.setContraseña("password123");
                testUser.setTipoUsuario("cliente");
                testUser.setTelefono("999999999");
                testUser.setActivo(true);
                testUser.setFechaCreacion(LocalDateTime.now());
                testUser.setCreatedAt(LocalDateTime.now());
                testUser.setUpdatedAt(LocalDateTime.now());
                
                testUser = usuarioRepository.save(testUser);
                System.out.println("Usuario de prueba creado con ID: " + testUser.getIdUsuario());
            }
            
            // Crear datos de prueba
            Map<String, Object> orderDetails = new HashMap<>();
            orderDetails.put("metodoEntrega", "tienda");
            orderDetails.put("nombreCompleto", "Cliente Test");
            orderDetails.put("telefonoContacto", "999999999");
            orderDetails.put("observaciones", "Prueba de debug");
            orderDetails.put("id_usuario", "1");
            orderDetails.put("totalGeneral", "S/ 25.00");
            
            // Productos de prueba
            List<Map<String, Object>> productos = List.of(
                Map.of("id", 1L, "nombre", "Producto Test (x1)", "precio", 25.0, "cantidad", 1)
            );
            orderDetails.put("productosComprados", productos);
            
            // Procesar venta
            Venta venta = ventaProcesamientoService.procesarVentaExitoso(orderDetails, "DEBUG_PAYMENT", "DEBUG_PAYER");
            
            return "Venta procesada exitosamente. ID: " + venta.getVentaId();
            
        } catch (Exception e) {
            return "Error: " + e.getMessage() + "\n" + e.getStackTrace()[0];
        }
    }

    /**
     * Procesa un pago simulado (tarjeta de crédito/débito)
     */
    @PostMapping("/procesar-pago-simulado")
    public RedirectView procesarPagoSimulado(
            @RequestParam(value = "metodo_entrega", required = false) String metodoEntrega,
            @RequestParam(value = "nombreCompleto", required = false) String nombreCompleto,
            @RequestParam(value = "direccion", required = false) String direccion,
            @RequestParam(value = "distrito", required = false) String distrito,
            @RequestParam(value = "ciudad", required = false) String ciudad,
            @RequestParam(value = "referencia", required = false) String referencia,
            @RequestParam(value = "telefonoContacto", required = false) String telefonoContacto,
            @RequestParam(value = "observaciones", required = false) String observaciones,
            @RequestParam(value = "cartData", required = false) String cartData,
            @RequestParam(value = "id_usuario", required = false) String idUsuario
    ) {
        try {
            // Simular procesamiento de pago exitoso
            System.out.println("=== PROCESANDO PAGO SIMULADO ===");
            System.out.println("Método entrega: " + metodoEntrega);
            System.out.println("Nombre completo: " + nombreCompleto);
            System.out.println("Teléfono: " + telefonoContacto);
            System.out.println("ID Usuario: " + idUsuario);
            System.out.println("Cart Data: " + cartData);
            
            // Verificar si hay un usuario válido
            Long userId = null;
            if (idUsuario != null && !idUsuario.isEmpty()) {
                try {
                    userId = Long.parseLong(idUsuario);
                } catch (NumberFormatException e) {
                    System.err.println("Error al parsear id_usuario: " + idUsuario);
                    userId = 1L; // Usar valor por defecto
                }
            } else {
                userId = 1L; // Usar valor por defecto
            }
            
            // Verificar si el usuario existe
            Usuario usuario = usuarioRepository.findById(userId).orElse(null);
            if (usuario == null) {
                System.err.println("Usuario no encontrado con ID: " + userId);
                // Crear un usuario de prueba si no existe
                usuario = new Usuario();
                usuario.setIdUsuario(userId);
                usuario.setNombre("Usuario Test");
                usuario.setEmail("test@farmacia.com");
                usuario.setContraseña("password123");
                usuario.setTipoUsuario("cliente");
                usuario.setTelefono("999999999");
                usuario.setActivo(true);
                usuario.setFechaCreacion(LocalDateTime.now());
                usuario.setCreatedAt(LocalDateTime.now());
                usuario.setUpdatedAt(LocalDateTime.now());
                
                usuario = usuarioRepository.save(usuario);
                System.out.println("Usuario de prueba creado con ID: " + usuario.getIdUsuario());
            } else {
                System.out.println("Usuario encontrado: " + usuario.getNombre());
            }
            
            // Crear detalles del pedido basados en los parámetros recibidos
            Map<String, Object> orderDetails = new HashMap<>();
            
            // Obtener datos del localStorage (esto se manejará en el frontend)
            // Por ahora, usamos los parámetros del formulario
            orderDetails.put("metodoEntrega", metodoEntrega);
            orderDetails.put("nombreCompleto", nombreCompleto);
            orderDetails.put("direccion", direccion);
            orderDetails.put("distrito", distrito);
            orderDetails.put("ciudad", ciudad);
            orderDetails.put("referencia", referencia);
            orderDetails.put("telefonoContacto", telefonoContacto);
            orderDetails.put("observaciones", observaciones);
            
            // Agregar datos del carrito si están disponibles
            if (cartData != null && !cartData.isEmpty()) {
                try {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> productos = (List<Map<String, Object>>) 
                        new com.fasterxml.jackson.databind.ObjectMapper().readValue(cartData, List.class);
                    orderDetails.put("productosComprados", productos);
                    System.out.println("Productos parseados: " + productos.size() + " productos");
                } catch (Exception e) {
                    System.err.println("Error al parsear datos del carrito: " + e.getMessage());
                    e.printStackTrace();
                    // Crear datos de prueba si no se pueden parsear
                    List<Map<String, Object>> productosPrueba = List.of(
                        Map.of("id", 1L, "nombre", "Producto Test (x1)", "precio", 25.0, "cantidad", 1)
                    );
                    orderDetails.put("productosComprados", productosPrueba);
                }
            } else {
                System.out.println("No hay datos del carrito, usando datos de prueba");
                // Crear datos de prueba
                List<Map<String, Object>> productosPrueba = List.of(
                    Map.of("id", 1L, "nombre", "Producto Test (x1)", "precio", 25.0, "cantidad", 1)
                );
                orderDetails.put("productosComprados", productosPrueba);
            }
            
            // Obtener id_usuario del parámetro recibido
            orderDetails.put("id_usuario", userId.toString());
            
            // Calcular total general
            double subtotal = 0.0;
            if (orderDetails.containsKey("productosComprados")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> productos = (List<Map<String, Object>>) orderDetails.get("productosComprados");
                for (Map<String, Object> producto : productos) {
                    double precio = Double.parseDouble(producto.get("precio").toString());
                    int cantidad = Integer.parseInt(producto.get("cantidad").toString());
                    subtotal += precio * cantidad;
                }
            }
            double costoEnvio = "delivery".equals(metodoEntrega) ? 5.00 : 0.00;
            double totalGeneral = subtotal + costoEnvio;
            
            orderDetails.put("totalGeneral", "S/ " + String.format("%.2f", totalGeneral));
            System.out.println("Total calculado: " + orderDetails.get("totalGeneral"));
            
            // Generar un ID de pago simulado
            String paymentId = "SIM_" + System.currentTimeMillis();
            String payerId = "SIM_PAYER_" + System.currentTimeMillis();
            
            // Procesar la venta
            System.out.println("Iniciando procesamiento de venta...");
            Venta ventaProcesada = ventaProcesamientoService.procesarVentaExitoso(orderDetails, paymentId, payerId);
            
            System.out.println("Venta procesada exitosamente con ID: " + ventaProcesada.getVentaId());
            
            // Redirigir a la página de éxito
            return new RedirectView("/compra");
            
        } catch (Exception e) {
            System.err.println("=== ERROR AL PROCESAR PAGO SIMULADO ===");
            System.err.println("Error: " + e.getMessage());
            System.err.println("Tipo de excepción: " + e.getClass().getSimpleName());
            e.printStackTrace();
            
            // En lugar de lanzar excepción, redirigir a una página de error
            try {
                return new RedirectView("/payment/general/error?message=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
            } catch (UnsupportedEncodingException encodingException) {
                return new RedirectView("/payment/general/error?message=Error+al+procesar+el+pago");
            }
        }
    }

    /**
     * Procesa una venta de PayPal después de un pago exitoso
     */
    @PostMapping("/procesar-venta-paypal")
    public RedirectView procesarVentaPaypal(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("payerId") String payerId,
            @RequestParam(value = "cartData", required = false) String cartData,
            @RequestParam(value = "metodo_entrega", required = false) String metodoEntrega,
            @RequestParam(value = "nombreCompleto", required = false) String nombreCompleto,
            @RequestParam(value = "direccion", required = false) String direccion,
            @RequestParam(value = "distrito", required = false) String distrito,
            @RequestParam(value = "ciudad", required = false) String ciudad,
            @RequestParam(value = "referencia", required = false) String referencia,
            @RequestParam(value = "telefonoContacto", required = false) String telefonoContacto,
            @RequestParam(value = "observaciones", required = false) String observaciones,
            @RequestParam(value = "id_usuario", required = false) String idUsuario
    ) {
        try {
            System.out.println("=== PROCESANDO VENTA PAYPAL ===");
            System.out.println("Payment ID: " + paymentId);
            System.out.println("Payer ID: " + payerId);
            System.out.println("Cart Data: " + cartData);
            System.out.println("Método entrega: " + metodoEntrega);
            System.out.println("Nombre completo: " + nombreCompleto);
            System.out.println("Teléfono: " + telefonoContacto);
            System.out.println("ID Usuario: " + idUsuario);
            
            // Crear detalles del pedido
            Map<String, Object> orderDetails = new HashMap<>();
            orderDetails.put("metodoEntrega", metodoEntrega);
            orderDetails.put("nombreCompleto", nombreCompleto);
            orderDetails.put("direccion", direccion);
            orderDetails.put("distrito", distrito);
            orderDetails.put("ciudad", ciudad);
            orderDetails.put("referencia", referencia);
            orderDetails.put("telefonoContacto", telefonoContacto);
            orderDetails.put("observaciones", observaciones);
            
            // Agregar datos del carrito si están disponibles
            if (cartData != null && !cartData.isEmpty()) {
                try {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> productos = (List<Map<String, Object>>) 
                        new com.fasterxml.jackson.databind.ObjectMapper().readValue(cartData, List.class);
                    orderDetails.put("productosComprados", productos);
                    System.out.println("Productos parseados: " + productos);
                } catch (Exception e) {
                    System.err.println("Error al parsear datos del carrito: " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException("Error al parsear datos del carrito: " + e.getMessage());
                }
            } else {
                System.err.println("No se recibieron datos del carrito");
                throw new RuntimeException("No se recibieron datos del carrito");
            }
            
            // Obtener id_usuario del parámetro recibido
            if (idUsuario == null || idUsuario.isEmpty()) {
                idUsuario = "1"; // Por defecto si no se proporciona
            }
            orderDetails.put("id_usuario", idUsuario);
            
            // Calcular total general
            double subtotal = 0.0;
            if (orderDetails.containsKey("productosComprados")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> productos = (List<Map<String, Object>>) orderDetails.get("productosComprados");
                for (Map<String, Object> producto : productos) {
                    double precio = Double.parseDouble(producto.get("precio").toString());
                    int cantidad = Integer.parseInt(producto.get("cantidad").toString());
                    subtotal += precio * cantidad;
                }
            }
            double costoEnvio = "delivery".equals(metodoEntrega) ? 5.00 : 0.00;
            double totalGeneral = subtotal + costoEnvio;
            
            orderDetails.put("totalGeneral", "S/ " + String.format("%.2f", totalGeneral));
            System.out.println("Total calculado: " + orderDetails.get("totalGeneral"));
            
            // Procesar la venta
            Venta ventaProcesada = ventaProcesamientoService.procesarVentaExitoso(orderDetails, paymentId, payerId);
            
            System.out.println("Venta de PayPal procesada exitosamente con ID: " + ventaProcesada.getVentaId());
            
            // Redirigir a la página de éxito
            return new RedirectView("/compra");
            
        } catch (Exception e) {
            System.err.println("=== ERROR AL PROCESAR VENTA PAYPAL ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            // En lugar de redirigir a error, devolver una respuesta de error más específica
            throw new RuntimeException("Error al procesar la venta: " + e.getMessage(), e);
        }
    }
} 