package com.proyecto.farmacia.webfarmacia.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
            System.out.println("=== PROCESANDO VENTA EXITOSA - INICIO ===");
            System.out.println("Order Details: " + orderDetails);
            System.out.println("Payment ID: " + paymentId);
            System.out.println("Payer ID: " + payerId);
            
            // Paso 1: Crear o obtener el cliente
            System.out.println("Paso 1: Obteniendo/creando cliente...");
            Cliente cliente = obtenerOCrearCliente(orderDetails);
            System.out.println("Cliente obtenido/creado: " + cliente.getNombre());
            
            // Paso 2: Obtener el usuario
            System.out.println("Paso 2: Obteniendo usuario...");
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
            
            if (idUsuario == null) {
                idUsuario = 1L;
                System.out.println("No se encontró id_usuario, usando usuario por defecto (ID: 1)");
            }
            
            System.out.println("Buscando usuario con ID: " + idUsuario);
            Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
            if (usuario == null) {
                System.err.println("Usuario no encontrado para id_usuario: " + idUsuario);
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
            
            // Paso 3: Crear la venta
            System.out.println("Paso 3: Creando venta...");
            Venta venta = new Venta();
            venta.setUsuario(usuario);
            venta.setFecha(LocalDateTime.now());
            venta.setMetodoPago("PAYPAL");
            venta.setEstado("PAGADA");
            venta.setReferenciaPago(paymentId);
            venta.setMetodoEntrega((String) orderDetails.getOrDefault("metodoEntrega", "tienda"));
            venta.setObservaciones((String) orderDetails.getOrDefault("observaciones", ""));
            
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
            
            // Paso 4: Guardar la venta
            System.out.println("Paso 4: Guardando venta en la base de datos...");
            System.out.println("Datos de la venta a guardar:");
            System.out.println("- Usuario ID: " + usuario.getIdUsuario());
            System.out.println("- Fecha: " + venta.getFecha());
            System.out.println("- Total: " + venta.getTotal());
            System.out.println("- Método Pago: " + venta.getMetodoPago());
            System.out.println("- Estado: " + venta.getEstado());
            System.out.println("- Referencia Pago: " + venta.getReferenciaPago());
            System.out.println("- Método Entrega: " + venta.getMetodoEntrega());
            System.out.println("- Dirección Entrega: " + venta.getDireccionEntrega());
            System.out.println("- Teléfono: " + venta.getTelefonoContacto());
            
            Venta ventaGuardada = ventaService.saveVenta(venta);
            System.out.println("✅ Venta guardada exitosamente con ID: " + ventaGuardada.getVentaId());
            
            if (ventaGuardada.getVentaId() == null) {
                throw new RuntimeException("Error: La venta no se guardó correctamente - ID es null");
            }
            
            // Paso 5: Procesar productos (simplificado)
            System.out.println("Paso 5: Procesando productos...");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> productos = (List<Map<String, Object>>) orderDetails.get("productosComprados");
            
            if (productos == null || productos.isEmpty()) {
                System.err.println("No se encontraron productos en los detalles del pedido");
                throw new RuntimeException("No se encontraron productos en los detalles del pedido");
            }
            
            System.out.println("Procesando " + productos.size() + " productos...");
            List<DetalleVenta> detallesVenta = new java.util.ArrayList<>();
            
            for (Map<String, Object> productoInfo : productos) {
                System.out.println("Procesando producto: " + productoInfo);
                // Aquí deberías obtener el producto real de la base de datos usando la información de productoInfo
                // Por ejemplo, si tienes el ID del producto:
                Long productoId = null;
                if (productoInfo.containsKey("productoId")) {
                    productoId = Long.valueOf(productoInfo.get("productoId").toString());
                } else if (productoInfo.containsKey("id")) {
                    productoId = Long.valueOf(productoInfo.get("id").toString());
                }
                Producto producto = null;
                if (productoId != null) {
                    producto = productoService.getProductoById(productoId);
                }
                if (producto == null) {
                    System.err.println("Producto no encontrado para el detalle de venta, omitiendo...");
                    continue;
                }
                // Crear detalle de venta
                System.out.println("Creando detalle de venta para: " + producto.getNombre());
                DetalleVenta detalleVenta = new DetalleVenta();
                detalleVenta.setVenta(ventaGuardada);
                detalleVenta.setProducto(producto);
                detalleVenta.setCantidad(1);
                detalleVenta.setPrecioUnitario(producto.getPrecio_unitario());
                detalleVenta.setCosteUnitario(producto.getCosteUnitario());
                System.out.println("Guardando detalle de venta...");
                DetalleVenta detalleGuardado = detalleVentaService.saveDetalleVenta(detalleVenta);
                if (detalleGuardado.getDetalleId() == null) {
                    throw new RuntimeException("Error: El detalle de venta no se guardó correctamente - ID es null");
                }
                System.out.println("✅ Detalle de venta guardado exitosamente con ID: " + detalleGuardado.getDetalleId());
                detallesVenta.add(detalleGuardado);
                System.out.println("Producto procesado: " + producto.getNombre());
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