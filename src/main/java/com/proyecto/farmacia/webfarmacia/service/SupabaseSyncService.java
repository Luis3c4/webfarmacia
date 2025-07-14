package com.proyecto.farmacia.webfarmacia.service;

import com.proyecto.farmacia.webfarmacia.model.Venta;
import com.proyecto.farmacia.webfarmacia.model.DetalleVenta;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
public class SupabaseSyncService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service.role.key}")
    private String serviceRoleKey;

    private final RestTemplate restTemplate;

    public SupabaseSyncService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Sincroniza una venta con Supabase
     */
    public boolean syncVentaToSupabase(Venta venta) {
        try {
            System.out.println("=== SINCRONIZANDO VENTA CON SUPABASE ===");
            System.out.println("Venta ID: " + venta.getVentaId());
            if (venta.getUsuario() != null) {
                System.out.println("Usuario: " + venta.getUsuario().toString());
            } else {
                System.out.println("Usuario: null");
            }
            System.out.println("Total: " + venta.getTotal());

            // Preparar headers para Supabase
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + serviceRoleKey);
            headers.set("apikey", serviceRoleKey);

            // Preparar datos de la venta para Supabase
            Map<String, Object> ventaData = new HashMap<>();
            ventaData.put("venta_id", venta.getVentaId());
            ventaData.put("id_usuario", venta.getUsuario().getIdUsuario());
            ventaData.put("total", venta.getTotal());
            ventaData.put("fecha", venta.getFecha().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            ventaData.put("metodo_pago", venta.getMetodoPago());
            ventaData.put("estado", venta.getEstado());
            ventaData.put("referencia_pago", venta.getReferenciaPago());
            ventaData.put("metodo_entrega", venta.getMetodoEntrega());
            ventaData.put("direccion_entrega", venta.getDireccionEntrega());
            ventaData.put("telefono_contacto", venta.getTelefonoContacto());
            ventaData.put("observaciones", venta.getObservaciones());
            ventaData.put("created_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            // URL para insertar en la tabla ventas de Supabase
            String ventasUrl = supabaseUrl + "/rest/v1/ventas";

            System.out.println("URL de ventas: " + ventasUrl);
            System.out.println("Datos de venta: " + ventaData);

            // Crear la entidad HTTP
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(ventaData, headers);

            // Realizar la petición POST a Supabase
            ResponseEntity<String> response = restTemplate.exchange(
                ventasUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
            );

            System.out.println("Respuesta de Supabase - Status: " + response.getStatusCode());
            System.out.println("Respuesta de Supabase - Body: " + response.getBody());

            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                System.out.println("✅ Venta sincronizada exitosamente con Supabase");
                return true;
            } else {
                System.err.println("❌ Error al sincronizar venta con Supabase. Status: " + response.getStatusCode());
                return false;
            }

        } catch (HttpClientErrorException e) {
            System.err.println("❌ Error HTTP 4xx al sincronizar venta: " + e.getMessage());
            System.err.println("Response body: " + e.getResponseBodyAsString());
            return false;
        } catch (HttpServerErrorException e) {
            System.err.println("❌ Error HTTP 5xx al sincronizar venta: " + e.getMessage());
            System.err.println("Response body: " + e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            System.err.println("❌ Error inesperado al sincronizar venta con Supabase: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sincroniza los detalles de una venta con Supabase
     */
    public boolean syncDetalleVentaToSupabase(List<DetalleVenta> detallesVenta) {
        try {
            System.out.println("=== SINCRONIZANDO DETALLES DE VENTA CON SUPABASE ===");
            System.out.println("Cantidad de detalles: " + detallesVenta.size());

            // Preparar headers para Supabase
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + serviceRoleKey);
            headers.set("apikey", serviceRoleKey);

            // URL para insertar en la tabla detalle_venta de Supabase
            String detallesUrl = supabaseUrl + "/rest/v1/detalle_venta";

            boolean allSuccess = true;

            // Insertar cada detalle de venta
            for (DetalleVenta detalle : detallesVenta) {
                try {
                    // Preparar datos del detalle para Supabase
                    Map<String, Object> detalleData = new HashMap<>();
                    detalleData.put("detalle_id", detalle.getDetalleId());
                    detalleData.put("venta_id", detalle.getVenta().getVentaId());
                    detalleData.put("producto_id", detalle.getProducto().getProductoId());
                    detalleData.put("cantidad", detalle.getCantidad());
                    detalleData.put("precio_unitario", detalle.getPrecioUnitario());
                    detalleData.put("coste_unitario", detalle.getCosteUnitario());
                    detalleData.put("descuentos", detalle.getDescuentos());
                    detalleData.put("created_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

                    System.out.println("Sincronizando detalle ID: " + detalle.getDetalleId());
                    System.out.println("Datos del detalle: " + detalleData);

                    // Crear la entidad HTTP
                    HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(detalleData, headers);

                    // Realizar la petición POST a Supabase
                    ResponseEntity<String> response = restTemplate.exchange(
                        detallesUrl,
                        HttpMethod.POST,
                        requestEntity,
                        String.class
                    );

                    System.out.println("Respuesta de detalle - Status: " + response.getStatusCode());
                    System.out.println("Respuesta de detalle - Body: " + response.getBody());

                    if (response.getStatusCode() != HttpStatus.CREATED && response.getStatusCode() != HttpStatus.OK) {
                        System.err.println("❌ Error al sincronizar detalle ID: " + detalle.getDetalleId());
                        allSuccess = false;
                    }

                } catch (Exception e) {
                    System.err.println("❌ Error al sincronizar detalle ID: " + detalle.getDetalleId() + ": " + e.getMessage());
                    allSuccess = false;
                }
            }

            if (allSuccess) {
                System.out.println("✅ Todos los detalles de venta sincronizados exitosamente con Supabase");
            } else {
                System.err.println("❌ Algunos detalles de venta no se pudieron sincronizar con Supabase");
            }

            return allSuccess;

        } catch (Exception e) {
            System.err.println("❌ Error inesperado al sincronizar detalles de venta con Supabase: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sincroniza una venta completa con todos sus detalles
     */
    public boolean syncVentaCompletaToSupabase(Venta venta, List<DetalleVenta> detallesVenta) {
        System.out.println("=== SINCRONIZACIÓN COMPLETA CON SUPABASE ===");
        
        // Primero sincronizar la venta
        boolean ventaSuccess = syncVentaToSupabase(venta);
        
        if (!ventaSuccess) {
            System.err.println("❌ No se pudo sincronizar la venta, abortando sincronización de detalles");
            return false;
        }

        // Luego sincronizar los detalles
        boolean detallesSuccess = syncDetalleVentaToSupabase(detallesVenta);
        
        boolean overallSuccess = ventaSuccess && detallesSuccess;
        
        if (overallSuccess) {
            System.out.println("✅ Sincronización completa exitosa con Supabase");
        } else {
            System.err.println("❌ Error en la sincronización completa con Supabase");
        }
        
        return overallSuccess;
    }

    /**
     * Verifica la conexión con Supabase
     */
    public boolean testSupabaseConnection() {
        try {
            System.out.println("=== PROBANDO CONEXIÓN CON SUPABASE ===");
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + serviceRoleKey);
            headers.set("apikey", serviceRoleKey);

            // Intentar hacer una consulta simple a la tabla ventas
            String testUrl = supabaseUrl + "/rest/v1/ventas?select=count&limit=1";
            
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                testUrl,
                HttpMethod.GET,
                requestEntity,
                String.class
            );

            System.out.println("Respuesta de prueba - Status: " + response.getStatusCode());
            System.out.println("Respuesta de prueba - Body: " + response.getBody());

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("✅ Conexión con Supabase exitosa");
                return true;
            } else {
                System.err.println("❌ Error en la conexión con Supabase. Status: " + response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Error al probar conexión con Supabase: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
} 