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
        return productoRepository.findAllActivos();
    }

    public Producto saveProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    public Producto getProductoById(Long id) {
        return productoRepository.findById(id).orElse(null);
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
        return productoRepository.findAllActivosPage(pageable);
    }
} 