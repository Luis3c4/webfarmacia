package com.proyecto.farmacia.webfarmacia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.proyecto.farmacia.webfarmacia.model.Producto;
import com.proyecto.farmacia.webfarmacia.repository.ProductoRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {
    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> getAllProductos() {
        return productoRepository.findByActivoTrue();
    }

    public Producto saveProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    public Producto getProductoById(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    public Producto getProductoBySupabaseId(String supabaseId) {
        return productoRepository.findBySupabaseId(supabaseId);
    }

    @Transactional
    public void deleteProducto(Long id) {
        Producto producto = productoRepository.findById(id).orElse(null);
        if (producto != null) {
            // Soft delete - marcar como inactivo
            producto.setActivo(false);
            productoRepository.save(producto);
        }
    }

    /*
    @Transactional
    public void deleteProductoFisico(Long id) {
        Producto producto = productoRepository.findById(id).orElse(null);
        if (producto != null) {
            // Eliminar registros relacionados primero
            if (producto.getDetalleVentas() != null) {
                producto.getDetalleVentas().clear();
            }
            if (producto.getDetalleCompras() != null) {
                producto.getDetalleCompras().clear();
            }
            productoRepository.deleteById(id);
        }
    }
    */

    public Page<Producto> getProductosPage(Pageable pageable) {
        return productoRepository.findByActivoTrue(pageable);
    }

    public List<Producto> getProductosByNombre(String nombre) {
        return productoRepository.findByActivoTrueAndNombreContainingIgnoreCase(nombre);
    }

    public Page<Producto> getProductosByNombrePage(String nombre, Pageable pageable) {
        return productoRepository.findByActivoTrueAndNombreContainingIgnoreCase(nombre, pageable);
    }

    public Producto getProductoByNombre(String nombre) {
        List<Producto> productos = productoRepository.findByActivoTrueAndNombreContainingIgnoreCase(nombre);
        if (!productos.isEmpty()) {
            return productos.get(0); // Retorna el primer producto que coincida
        }
        return null;
    }
    
    public Page<Producto> getProductosByCategoriaPage(String categoria, Pageable pageable) {
        return productoRepository.findByActivoTrueAndCategoriaContainingIgnoreCase(categoria, pageable);
    }
    
    public List<String> getCategoriasDisponibles() {
        return productoRepository.findDistinctCategoriasByActivoTrue();
    }
    
    /**
     * Actualiza el stock de un producto y su estado correspondiente
     * @param productoId ID del producto
     * @param cantidadVendida Cantidad que se va a descontar del stock
     * @return true si se actualizó correctamente, false si no hay stock suficiente
     */
    @Transactional
    public boolean actualizarStockProducto(Long productoId, int cantidadVendida) {
        try {
            Producto producto = productoRepository.findById(productoId).orElse(null);
            if (producto == null) {
                System.err.println("Producto no encontrado con ID: " + productoId);
                return false;
            }
            
            int stockActual = producto.getStock();
            if (stockActual < cantidadVendida) {
                System.err.println("Stock insuficiente para producto " + producto.getNombre() + 
                                 " (disponible: " + stockActual + ", solicitado: " + cantidadVendida + ")");
                return false;
            }
            
            // Descontar la cantidad vendida
            int nuevoStock = stockActual - cantidadVendida;
            producto.setStock(nuevoStock);
            
            // Actualizar el estado del stock automáticamente
            if (nuevoStock <= 0) {
                producto.setStockStatus("OUT_OF_STOCK");
            } else if (nuevoStock <= 10) {
                producto.setStockStatus("LOW_STOCK");
            } else {
                producto.setStockStatus("IN_STOCK");
            }
            
            // Actualizar timestamp
            producto.setUpdatedAt(java.time.LocalDateTime.now());
            
            productoRepository.save(producto);
            
            System.out.println("Stock actualizado para producto " + producto.getNombre() + 
                             ": " + stockActual + " -> " + nuevoStock + 
                             " (vendido: " + cantidadVendida + ")");
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error al actualizar stock del producto " + productoId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Verifica si hay stock suficiente para un producto
     * @param productoId ID del producto
     * @param cantidadSolicitada Cantidad que se quiere comprar
     * @return true si hay stock suficiente, false en caso contrario
     */
    public boolean verificarStockDisponible(Long productoId, int cantidadSolicitada) {
        Producto producto = productoRepository.findById(productoId).orElse(null);
        if (producto == null) {
            return false;
        }
        return producto.getStock() >= cantidadSolicitada;
    }
} 