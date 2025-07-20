package com.proyecto.farmacia.webfarmacia.controller;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.farmacia.webfarmacia.model.Usuario;
import com.proyecto.farmacia.webfarmacia.model.Venta;
import com.proyecto.farmacia.webfarmacia.repository.UsuarioRepository;
import com.proyecto.farmacia.webfarmacia.service.DetalleVentaService;
import com.proyecto.farmacia.webfarmacia.service.VentaProcesamientoService;

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

    @Autowired
    private DetalleVentaService detalleVentaService;

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
                    // No agregar productos de prueba si no se pueden parsear
                }
            } else {
                System.out.println("No hay datos del carrito, no se agregan productos de prueba");
                // No agregar productos de prueba
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
    @Transactional
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
            System.out.println("=== PROCESANDO VENTA PAYPAL - INICIO ===");
            System.out.println("Payment ID: " + paymentId);
            System.out.println("Payer ID: " + payerId);
            System.out.println("Cart Data: " + cartData);
            System.out.println("Método entrega: " + metodoEntrega);
            System.out.println("Nombre completo: " + nombreCompleto);
            System.out.println("Teléfono: " + telefonoContacto);
            System.out.println("ID Usuario: " + idUsuario);
            
            // Paso 1: Verificar usuario
            System.out.println("Paso 1: Verificando usuario...");
            Long userId = 1L; // Usar valor por defecto
            if (idUsuario != null && !idUsuario.isEmpty()) {
                try {
                    userId = Long.parseLong(idUsuario);
                } catch (NumberFormatException e) {
                    System.err.println("Error al parsear id_usuario: " + idUsuario + ", usando valor por defecto");
                }
            }
            
            Usuario usuario = usuarioRepository.findById(userId).orElse(null);
            if (usuario == null) {
                System.out.println("Usuario no encontrado, creando usuario de prueba...");
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
            
            // Paso 2: Crear datos del pedido
            System.out.println("Paso 2: Creando datos del pedido...");
            Map<String, Object> orderDetails = new HashMap<>();
            orderDetails.put("metodoEntrega", metodoEntrega != null ? metodoEntrega : "tienda");
            orderDetails.put("nombreCompleto", nombreCompleto != null ? nombreCompleto : "Cliente Test");
            orderDetails.put("direccion", direccion);
            orderDetails.put("distrito", distrito);
            orderDetails.put("ciudad", ciudad);
            orderDetails.put("referencia", referencia);
            orderDetails.put("telefonoContacto", telefonoContacto != null ? telefonoContacto : "999999999");
            orderDetails.put("observaciones", observaciones);
            orderDetails.put("id_usuario", userId.toString());
            
            // Paso 3: Procesar datos del carrito
            System.out.println("Paso 3: Procesando datos del carrito...");
            if (cartData != null && !cartData.isEmpty()) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> productos = mapper.readValue(cartData, List.class);
                    orderDetails.put("productosComprados", productos);
                    System.out.println("Productos parseados: " + productos.size() + " productos");
                } catch (Exception e) {
                    System.err.println("Error al parsear datos del carrito: " + e.getMessage());
                    // No agregar productos de prueba, solo registrar el error
                }
            } else {
                System.out.println("No hay datos del carrito, no se agregan productos de prueba");
                // No agregar productos de prueba
            }
            
            // Paso 4: Calcular total
            System.out.println("Paso 4: Calculando total...");
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
            
            // Paso 5: Procesar la venta
            System.out.println("Paso 5: Iniciando procesamiento de venta...");
            Venta ventaProcesada = ventaProcesamientoService.procesarVentaExitoso(orderDetails, paymentId, payerId);
            
            System.out.println("✅ Venta de PayPal procesada exitosamente con ID: " + ventaProcesada.getVentaId());
            System.out.println("=== PROCESANDO VENTA PAYPAL - FINALIZADO ===");
            
            // Redirigir a la página de éxito
            return new RedirectView("/compra");
            
        } catch (Exception e) {
            System.err.println("=== ERROR AL PROCESAR VENTA PAYPAL ===");
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
} 