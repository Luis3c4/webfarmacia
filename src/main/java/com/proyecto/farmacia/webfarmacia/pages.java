package com.proyecto.farmacia.webfarmacia;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.proyecto.farmacia.webfarmacia.model.Producto;
import com.proyecto.farmacia.webfarmacia.service.ProductoService;

@Controller
public class pages {
    
    @Autowired
    private ProductoService productoService;
    
    @GetMapping({ "/", "/inicio" }) // Puedes mapear la raíz "/" y "/inicio" al mismo método si quieres
    public String mostrarPaginaInicio() { // Nombre de método descriptivo
        return "inicio"; // Devuelve el nombre de la plantilla "inicio.html"
    }

    @GetMapping("/pasarela")
    public String mostrarPasarela() {
        // Asumiendo que tienes una plantilla llamada "pasarela.html" en
        // src/main/resources/templates/
        return "pasarela";
    }

    @GetMapping("/productos") // Cambiado a "/productos" para que coincida con tu HTML
    public String mostrarPaginaProductos(Model model) { // Nombre de método descriptivo
        // Obtener todos los productos activos de la base de datos
        var productos = productoService.getAllProductos();
        model.addAttribute("productos", productos);
        return "productos"; // Devuelve el nombre de la plantilla "productos.html"
    }

    // Si tienes una página de contacto:
    @GetMapping("/contacto")
    public String mostrarPaginaContacto() {
        // Asumiendo que tienes una plantilla llamada "contacto.html" en
        // src/main/resources/templates/
        return "contacto";
    }

    @GetMapping("/registro")
    public String mostrarPaginaRegistro() {
        // Asumiendo que tienes una plantilla llamada "contacto.html" en
        // src/main/resources/templates/
        return "registro";
    }

    @GetMapping("/login")
    public String mostrarPaginaLogin() {
        // Asumiendo que tienes una plantilla llamada "contacto.html" en
        // src/main/resources/templates/
        return "login";
    }

    @GetMapping("/carrito")
    public String mostrarCarrito() {
        return "carrito";
    }

    @GetMapping("/compra")
    public String mostrarCompra() {
        return "compra";
    }

    @GetMapping("/producto/detalle/{id}")
    public String mostrarDetalleProductoCliente(@PathVariable Long id, Model model) {
        // Obtener el producto por ID desde la base de datos
        Producto producto = productoService.getProductoById(id);
        
        if (producto != null && producto.isActivo()) {
            // Log para verificar que todos los datos se están obteniendo correctamente
            System.out.println("=== DETALLE DEL PRODUCTO ===");
            System.out.println("ID: " + producto.getProductoId());
            System.out.println("Nombre: " + producto.getNombre());
            System.out.println("Precio: " + producto.getPrecio_unitario());
            System.out.println("Stock: " + producto.getStock());
            System.out.println("Categoría: " + producto.getCategoria());
            System.out.println("Descripción: " + producto.getDescripcion());
            System.out.println("Imagen URL: " + producto.getImgUrl());
            System.out.println("Cantidad Ingresada: " + producto.getCantidadIngresada());
            System.out.println("Estado Stock: " + producto.getStockStatus());
            System.out.println("=============================");
            
            // Obtener productos sugeridos (excluyendo el producto actual)
            List<Producto> productosSugeridos = productoService.getAllProductos()
                .stream()
                .filter(p -> !p.getProductoId().equals(id) && p.isActivo())
                .limit(5)
                .toList();
            
            model.addAttribute("producto", producto);
            model.addAttribute("productosSugeridos", productosSugeridos);
            return "productos_detalle";
        } else {
            // Si el producto no existe o no está activo, redirigir a productos
            System.out.println("Producto no encontrado o inactivo con ID: " + id);
            return "redirect:/productos";
        }
    }



    @GetMapping("/producto/detalle/supabase/{uuid}")
    public String mostrarDetalleProductoPorSupabaseId(@PathVariable String uuid, Model model) {
        Producto producto = productoService.getProductoBySupabaseId(uuid);
        if (producto != null && producto.isActivo()) {
            model.addAttribute("producto", producto);
            return "productos_detalle";
        } else {
            return "redirect:/productos";
        }
    }
    
    // Endpoint de prueba para verificar datos de Supabase
    @GetMapping("/test/productos")
    public String testProductos(Model model) {
        List<Producto> productos = productoService.getAllProductos();
        System.out.println("=== PRODUCTOS EN SUPABASE ===");
        productos.forEach(producto -> {
            System.out.println("ID: " + producto.getProductoId() + 
                             " | Nombre: " + producto.getNombre() + 
                             " | Precio: " + producto.getPrecio_unitario() + 
                             " | Imagen: " + producto.getImgUrl());
        });
        System.out.println("Total productos: " + productos.size());
        System.out.println("=============================");
        
        model.addAttribute("productos", productos);
        return "productos";
    }
    
    @GetMapping("/test/usuarios")
    public String testUsuarios() {
        return "test-usuarios";
    }
    
    @GetMapping("/test/db")
    public String testDb() {
        return "test-db";
    }
    
    @GetMapping("/test/productos-simple")
    public String testProductosSimple() {
        return "test-productos";
    }
    
    @GetMapping("/test/login-debug")
    public String testLoginDebug() {
        return "test-login-debug";
    }

    @GetMapping("/payment/general/error")
    public String mostrarErrorPago() {
        return "paymentError";
    }
}
