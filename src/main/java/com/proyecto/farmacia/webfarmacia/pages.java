package com.proyecto.farmacia.webfarmacia;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class pages {
    @GetMapping({ "/", "/inicio" }) // Puedes mapear la raíz "/" y "/inicio" al mismo método si quieres
    public String mostrarPaginaInicio() { // Nombre de método descriptivo
        return "inicio"; // Devuelve el nombre de la plantilla "inicio.html"
    }

    @GetMapping("/productos") // Cambiado a "/productos" para que coincida con tu HTML
    public String mostrarPaginaProductos() { // Nombre de método descriptivo
        // Asumiendo que tienes una plantilla llamada "productos.html" en
        // src/main/resources/templates/
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

    @GetMapping("/pasarela")
    public String mostrarPasarela() {
        // Asumiendo que tienes una plantilla llamada "contacto.html" en
        // src/main/resources/templates/
        return "pasarela";
    }

    @GetMapping("/detalle_producto")
    public String mostrarDetalleProducto() {
        // Asumiendo que tienes una plantilla llamada "contacto.html" en
        // src/main/resources/templates/
        return "detalle_producto";
    }

    @GetMapping("/compra")
    public String mostrarDetalleCompra() {
        // Asumiendo que tienes una plantilla llamada "contacto.html" en
        // src/main/resources/templates/
        return "compra";
    }
}
