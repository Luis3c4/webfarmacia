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
public class PagesController {
    
    @Autowired
    private ProductoService productoService;
    
    @GetMapping({ "/", "/inicio" })
    public String mostrarPaginaInicio() {
        return "inicio";
    }

    @GetMapping("/pasarela")
    public String mostrarPasarela() {
        return "pasarela";
    }

    @GetMapping("/productos")
    public String mostrarPaginaProductos(Model model) {
        var productos = productoService.getAllProductos();
        model.addAttribute("productos", productos);
        return "productos";
    }

    @GetMapping("/contacto")
    public String mostrarPaginaContacto() {
        return "contacto";
    }

    @GetMapping("/registro")
    public String mostrarPaginaRegistro() {
        return "registro";
    }

    @GetMapping("/login")
    public String mostrarPaginaLogin() {
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
        Producto producto = productoService.getProductoById(id);
        if (producto != null && producto.isActivo()) {
            List<Producto> productosSugeridos = productoService.getAllProductos()
                .stream()
                .filter(p -> !p.getProductoId().equals(id) && p.isActivo())
                .limit(5)
                .toList();
            model.addAttribute("producto", producto);
            model.addAttribute("productosSugeridos", productosSugeridos);
            return "productos_detalle";
        } else {
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

    @GetMapping("/payment/general/error")
    public String mostrarErrorPago() {
        return "paymentError";
    }
}
