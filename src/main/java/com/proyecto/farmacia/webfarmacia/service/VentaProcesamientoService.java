package com.proyecto.farmacia.webfarmacia.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.farmacia.webfarmacia.model.Cliente;
import com.proyecto.farmacia.webfarmacia.model.ClienteStatus;
import com.proyecto.farmacia.webfarmacia.model.DetalleVenta;
import com.proyecto.farmacia.webfarmacia.model.Producto;
import com.proyecto.farmacia.webfarmacia.model.Usuario;
import com.proyecto.farmacia.webfarmacia.model.Venta;
import com.proyecto.farmacia.webfarmacia.repository.UsuarioRepository;

@Service
public class VentaProcesamientoService {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private DetalleVentaService detalleVentaService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SupabaseSyncService supabaseSyncService;

    /**
     * Procesa una venta después de un pago exitoso
     * @param orderDetails Detalles del pedido desde localStorage
     * @param paymentId ID del pago de PayPal
     * @param payerId ID del pagador de PayPal
     * @return La venta procesada
     */
    @Transactional
    public Venta procesarVentaExitoso(Map<String, Object> orderDetails, String paymentId, String payerId) {
        try {
            System.out.println("=== PROCESANDO VENTA EXITOSA ===");
            System.out.println("Order Details: " + orderDetails);
            System.out.println("Payment ID: " + paymentId);
            System.out.println("Payer ID: " + payerId);
            
            // Crear o obtener el cliente
            System.out.println("Obteniendo/creando cliente...");
            Cliente cliente = obtenerOCrearCliente(orderDetails);
            System.out.println("Cliente obtenido/creado: " + cliente.getNombre());
            
            // Obtener el usuario por id_usuario
            Long idUsuario = null;
            if (orderDetails.containsKey("id_usuario")) {
                Object idObj = orderDetails.get("id_usuario");
                if (idObj instanceof Number) {
                    idUsuario = ((Number) idObj).longValue();
                } else if (idObj instanceof String) {
                    try { 
                        idUsuario = Long.parseLong((String) idObj); 
                        System.out.println("ID Usuario parseado: " + idUsuario);
                    } catch (Exception e) {
                        System.err.println("Error al parsear id_usuario: " + idObj);
                        idUsuario = 1L;
                    }
                }
            }
            
            // Si no hay id_usuario, usar un usuario por defecto (ID 1)
            if (idUsuario == null) {
                idUsuario = 1L;
                System.out.println("No se encontró id_usuario, usando usuario por defecto (ID: 1)");
            }
            
            System.out.println("Buscando usuario con ID: " + idUsuario);
            Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
            if (usuario == null) {
                System.err.println("Usuario no encontrado para id_usuario: " + idUsuario);
                // Crear un usuario de prueba si no existe
                System.out.println("Creando usuario de prueba...");
                usuario = new Usuario();
                usuario.setIdUsuario(idUsuario);
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
            
            // Crear la venta
            System.out.println("Creando venta...");
            Venta venta = new Venta();
            venta.setUsuario(usuario);
            venta.setFecha(LocalDateTime.now());
            venta.setMetodoPago("PAYPAL");
            venta.setEstado("PAGADA");
            venta.setReferenciaPago(paymentId); // Usar el paymentId de PayPal
            venta.setMetodoEntrega((String) orderDetails.getOrDefault("metodoEntrega", "tienda"));
            venta.setObservaciones((String) orderDetails.getOrDefault("observaciones", ""));
            
            // Construir dirección de entrega completa
            String direccionEntrega = construirDireccionEntrega(orderDetails);
            venta.setDireccionEntrega(direccionEntrega);
            
            venta.setTelefonoContacto((String) orderDetails.getOrDefault("telefonoContacto", ""));
            venta.setCreatedAt(LocalDateTime.now());
            venta.setUpdatedAt(LocalDateTime.now());
            
            // Calcular el total
            String totalStr = (String) orderDetails.get("totalGeneral");
            if (totalStr == null || totalStr.isEmpty()) {
                System.err.println("No se encontró el total general en los detalles del pedido");
                throw new RuntimeException("No se encontró el total general en los detalles del pedido");
            }
            BigDecimal total = new BigDecimal(totalStr.replace("S/ ", "").replace(",", ""));
            venta.setTotal(total);
            System.out.println("Total de la venta: " + total);
            
            // Guardar la venta
            System.out.println("Guardando venta en la base de datos...");
            Venta ventaGuardada = ventaService.saveVenta(venta);
            System.out.println("Venta guardada con ID: " + ventaGuardada.getVentaId());
            
            // Procesar los productos del carrito
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> productos = (List<Map<String, Object>>) orderDetails.get("productosComprados");
            
            if (productos == null || productos.isEmpty()) {
                System.err.println("No se encontraron productos en los detalles del pedido");
                throw new RuntimeException("No se encontraron productos en los detalles del pedido");
            }
            
            System.out.println("Procesando " + productos.size() + " productos...");
            List<DetalleVenta> detallesVenta = new java.util.ArrayList<>();
            
            // Validar stock antes de procesar la venta
            System.out.println("Validando stock disponible...");
            for (Map<String, Object> productoInfo : productos) {
                Long idProducto = null;
                if (productoInfo.containsKey("id")) {
                    Object idObj = productoInfo.get("id");
                    if (idObj instanceof Number) {
                        idProducto = ((Number) idObj).longValue();
                    } else if (idObj instanceof String) {
                        try { idProducto = Long.parseLong((String) idObj); } catch (Exception ignore) {}
                    }
                }
                
                if (idProducto != null) {
                    int cantidad = 1;
                    if (productoInfo.containsKey("cantidad")) {
                        Object cantidadObj = productoInfo.get("cantidad");
                        if (cantidadObj instanceof Number) {
                            cantidad = ((Number) cantidadObj).intValue();
                        } else if (cantidadObj instanceof String) {
                            try { cantidad = Integer.parseInt((String) cantidadObj); } catch (Exception e) {
                                cantidad = 1;
                            }
                        }
                    }
                    
                    if (!productoService.verificarStockDisponible(idProducto, cantidad)) {
                        Producto producto = productoService.getProductoById(idProducto);
                        String nombreProducto = producto != null ? producto.getNombre() : "Producto ID " + idProducto;
                        String errorMsg = "Stock insuficiente para: " + nombreProducto + 
                                        " (solicitado: " + cantidad + ")";
                        System.err.println(errorMsg);
                        throw new RuntimeException(errorMsg);
                    }
                }
            }
            System.out.println("✅ Validación de stock completada - hay suficiente stock para todos los productos");
            
            for (Map<String, Object> productoInfo : productos) {
                System.out.println("Procesando producto: " + productoInfo);
                
                // Usar id si está disponible
                Long idProducto = null;
                if (productoInfo.containsKey("id")) {
                    Object idObj = productoInfo.get("id");
                    if (idObj instanceof Number) {
                        idProducto = ((Number) idObj).longValue();
                    } else if (idObj instanceof String) {
                        try { idProducto = Long.parseLong((String) idObj); } catch (Exception ignore) {}
                    }
                }
                
                Producto producto = null;
                String nombreProducto = (String) productoInfo.get("nombre");
                
                // CORRECCIÓN: Usar la cantidad directamente del objeto del carrito
                int cantidad = 1; // valor por defecto
                if (productoInfo.containsKey("cantidad")) {
                    Object cantidadObj = productoInfo.get("cantidad");
                    if (cantidadObj instanceof Number) {
                        cantidad = ((Number) cantidadObj).intValue();
                    } else if (cantidadObj instanceof String) {
                        try { cantidad = Integer.parseInt((String) cantidadObj); } catch (Exception e) {
                            System.err.println("Error al parsear cantidad: " + cantidadObj + ", usando valor por defecto 1");
                            cantidad = 1;
                        }
                    }
                } else {
                    // Si no hay cantidad en el objeto, intentar extraerla del nombre como fallback
                    cantidad = extraerCantidad(nombreProducto);
                }
                
                // Limpiar el nombre del producto si contiene la cantidad
                String nombreReal = extraerNombreReal(nombreProducto);
                
                System.out.println("Buscando producto con ID: " + idProducto + " o nombre: " + nombreReal + " (cantidad: " + cantidad + ")");
                
                if (idProducto != null) {
                    producto = productoService.getProductoById(idProducto);
                    if (producto != null) {
                        System.out.println("Producto encontrado por ID: " + producto.getNombre());
                    }
                } else {
                    producto = productoService.getProductoByNombre(nombreReal);
                    if (producto != null) {
                        System.out.println("Producto encontrado por nombre: " + producto.getNombre());
                    }
                }
                
                // Si no se encuentra el producto, crear uno de prueba
                if (producto == null) {
                    System.out.println("Producto no encontrado, creando producto de prueba...");
                    producto = new Producto();
                    producto.setNombre(nombreReal != null ? nombreReal : "Producto Test");
                    producto.setDescripcion("Producto de prueba creado automáticamente");
                    producto.setPrecio_unitario(BigDecimal.valueOf(25.0));
                    producto.setStock(100);
                    producto.setCantidadIngresada(100);
                    producto.setCategoria("General");
                    producto.setCosteUnitario(BigDecimal.valueOf(15.0));
                    producto.setStockStatus("IN_STOCK");
                    producto.setActivo(true);
                    producto.setFechaProducto(LocalDate.now());
                    producto.setCreatedAt(LocalDateTime.now());
                    producto.setUpdatedAt(LocalDateTime.now());
                    
                    producto = productoService.saveProducto(producto);
                    System.out.println("Producto de prueba creado con ID: " + producto.getProductoId());
                }
                
                if (producto.getStock() >= cantidad) {
                    // Crear detalle de venta
                    System.out.println("Creando detalle de venta para: " + producto.getNombre() + " (cantidad: " + cantidad + ")");
                    DetalleVenta detalleVenta = new DetalleVenta();
                    detalleVenta.setVenta(ventaGuardada);
                    detalleVenta.setProducto(producto);
                    detalleVenta.setCantidad(cantidad);
                    detalleVenta.setPrecioUnitario(producto.getPrecio_unitario());
                    detalleVenta.setCosteUnitario(producto.getCosteUnitario());
                    
                    // Guardar detalle de venta
                    DetalleVenta detalleGuardado = detalleVentaService.saveDetalleVenta(detalleVenta);
                    detallesVenta.add(detalleGuardado);
                    
                    // Actualizar stock del producto
                    actualizarStockProducto(producto, cantidad);
                    
                    System.out.println("Producto procesado: " + producto.getNombre() + " (cantidad: " + cantidad + ")");
                } else {
                    String errorMsg = "Stock insuficiente para: " + producto.getNombre() + 
                                    " (disponible: " + producto.getStock() + ", solicitado: " + cantidad + ")";
                    System.err.println(errorMsg);
                    throw new RuntimeException(errorMsg);
                }
            }
            
            // Sincronizar con Supabase
            try {
                System.out.println("Sincronizando con Supabase...");
                boolean syncSuccess = supabaseSyncService.syncVentaCompletaToSupabase(ventaGuardada, detallesVenta);
                if (syncSuccess) {
                    System.out.println("✅ Venta y detalles sincronizados exitosamente con Supabase");
                } else {
                    System.err.println("⚠️ Advertencia: La venta se guardó localmente pero hubo problemas al sincronizar con Supabase");
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error al sincronizar con Supabase: " + e.getMessage());
                e.printStackTrace();
                // No lanzar excepción para no afectar la venta local
            }
            
            System.out.println("=== VENTA PROCESADA EXITOSAMENTE ===");
            return ventaGuardada;
            
        } catch (Exception e) {
            System.err.println("=== ERROR AL PROCESAR LA VENTA ===");
            System.err.println("Error: " + e.getMessage());
            System.err.println("Tipo de excepción: " + e.getClass().getSimpleName());
            e.printStackTrace();
            throw new RuntimeException("Error al procesar la venta: " + e.getMessage(), e);
        }
    }

    /**
     * Construye la dirección de entrega completa a partir de los campos individuales
     */
    private String construirDireccionEntrega(Map<String, Object> orderDetails) {
        StringBuilder direccion = new StringBuilder();
        
        String metodoEntrega = (String) orderDetails.get("metodoEntrega");
        if ("tienda".equals(metodoEntrega)) {
            return "Recojo en tienda - Av. El Sol 123, San Juan de Lurigancho, Lima (UTP SJL)";
        }
        
        // Para delivery, construir dirección completa
        String nombreCompleto = (String) orderDetails.get("nombreCompleto");
        String direccionStr = (String) orderDetails.get("direccion");
        String distrito = (String) orderDetails.get("distrito");
        String ciudad = (String) orderDetails.get("ciudad");
        String referencia = (String) orderDetails.get("referencia");
        
        if (nombreCompleto != null && !nombreCompleto.isEmpty()) {
            direccion.append("Destinatario: ").append(nombreCompleto).append(" | ");
        }
        
        if (direccionStr != null && !direccionStr.isEmpty()) {
            direccion.append(direccionStr);
        }
        
        if (distrito != null && !distrito.isEmpty()) {
            if (direccion.length() > 0) direccion.append(", ");
            direccion.append(distrito);
        }
        
        if (ciudad != null && !ciudad.isEmpty()) {
            if (direccion.length() > 0) direccion.append(", ");
            direccion.append(ciudad);
        }
        
        if (referencia != null && !referencia.isEmpty()) {
            direccion.append(" | Ref: ").append(referencia);
        }
        
        return direccion.toString();
    }

    /**
     * Obtiene o crea un cliente basado en los detalles del pedido
     */
    private Cliente obtenerOCrearCliente(Map<String, Object> orderDetails) {
        // Buscar nombreCliente o nombreCompleto
        String nombreCliente = (String) orderDetails.get("nombreCliente");
        if (nombreCliente == null || nombreCliente.isEmpty()) {
            nombreCliente = (String) orderDetails.get("nombreCompleto");
        }
        
        String telefono = (String) orderDetails.get("telefonoContacto");
        
        if (nombreCliente == null || nombreCliente.isEmpty()) {
            nombreCliente = "Cliente Anónimo";
        }
        
        if (telefono == null || telefono.isEmpty()) {
            telefono = "000000000";
        }
        
        // Buscar cliente por teléfono
        List<Cliente> clientesExistentes = clienteService.getClientesByTelefono(telefono);
        
        if (!clientesExistentes.isEmpty()) {
            return clientesExistentes.get(0);
        }
        
        // Crear nuevo cliente
        Cliente nuevoCliente = new Cliente();
        nuevoCliente.setNombre(nombreCliente);
        nuevoCliente.setNumeroContacto(telefono);
        nuevoCliente.setEmail("cliente@temporal.com"); // Email temporal
        nuevoCliente.setProducto("General"); // Producto general
        nuevoCliente.setEstado(ClienteStatus.NOT_TAKING_RETURN); // Estado por defecto
        
        return clienteService.saveCliente(nuevoCliente);
    }

    /**
     * Extrae la cantidad del nombre del producto (formato: "Nombre (x2)")
     */
    private int extraerCantidad(String nombreProducto) {
        try {
            int startIndex = nombreProducto.lastIndexOf("(x");
            int endIndex = nombreProducto.lastIndexOf(")");
            if (startIndex != -1 && endIndex != -1) {
                String cantidadStr = nombreProducto.substring(startIndex + 2, endIndex);
                return Integer.parseInt(cantidadStr);
            }
        } catch (Exception e) {
            // Si no se puede parsear, asumir cantidad 1
        }
        return 1;
    }

    /**
     * Extrae el nombre real del producto sin la cantidad
     */
    private String extraerNombreReal(String nombreProducto) {
        int startIndex = nombreProducto.lastIndexOf("(x");
        if (startIndex != -1) {
            return nombreProducto.substring(0, startIndex).trim();
        }
        return nombreProducto;
    }

    /**
     * Actualiza el stock de un producto
     */
    @Transactional
    private void actualizarStockProducto(Producto producto, int cantidadVendida) {
        try {
            System.out.println("Actualizando stock para producto: " + producto.getNombre() + 
                             " (ID: " + producto.getProductoId() + ", cantidad vendida: " + cantidadVendida + ")");
            
            // Usar el nuevo método del ProductoService
            boolean actualizacionExitosa = productoService.actualizarStockProducto(producto.getProductoId(), cantidadVendida);
            
            if (!actualizacionExitosa) {
                String errorMsg = "No se pudo actualizar el stock del producto " + producto.getNombre() + 
                                " (ID: " + producto.getProductoId() + ")";
                System.err.println(errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
            System.out.println("✅ Stock actualizado exitosamente para producto: " + producto.getNombre());
            
        } catch (Exception e) {
            System.err.println("❌ Error al actualizar stock del producto " + producto.getNombre() + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar stock del producto: " + e.getMessage(), e);
        }
    }
} 