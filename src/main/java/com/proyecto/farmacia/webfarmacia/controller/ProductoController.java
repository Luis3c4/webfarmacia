package com.proyecto.farmacia.webfarmacia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.proyecto.farmacia.webfarmacia.model.Producto;
import com.proyecto.farmacia.webfarmacia.service.ProductoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@RestController
@RequestMapping("/api/productos")
@EnableSpringDataWebSupport
public class ProductoController {
    @Autowired
    private ProductoService productoService;

    @PostMapping
    public ResponseEntity<Producto> createProducto(@RequestBody Producto producto) {
        Producto nuevoProducto = productoService.saveProducto(producto);
        return ResponseEntity.ok(nuevoProducto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> getProductoById(@PathVariable Long id) {
        Producto producto = productoService.getProductoById(id);
        if (producto != null) {
            return ResponseEntity.ok(producto);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> updateProducto(@PathVariable Long id, @RequestBody Producto producto) {
        Producto productoExistente = productoService.getProductoById(id);
        if (productoExistente == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Actualizar los campos del producto existente
        productoExistente.setNombre(producto.getNombre());
        productoExistente.setPrecio_unitario(producto.getPrecio_unitario());
        productoExistente.setCantidadIngresada(producto.getCantidadIngresada());
        productoExistente.setDescripcion(producto.getDescripcion());
        productoExistente.setStock(producto.getStock());
        
        Producto productoActualizado = productoService.saveProducto(productoExistente);
        return ResponseEntity.ok(productoActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProducto(@PathVariable Long id) {
        productoService.deleteProducto(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public Page<Producto> getProductosPage(@PageableDefault(size = 10) Pageable pageable) {
        return productoService.getProductosPage(pageable);
    }
} 