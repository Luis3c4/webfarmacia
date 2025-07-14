package com.proyecto.farmacia.webfarmacia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.proyecto.farmacia.webfarmacia.model.Usuario;
import com.proyecto.farmacia.webfarmacia.service.UsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public Page<Usuario> getClientesPage(@PageableDefault(size = 10) Pageable pageable) {
        return usuarioService.getUsuariosByTipoPage("clientes", pageable);
    }

    @GetMapping("/buscar")
    public Page<Usuario> getClientesByNombre(@RequestParam String nombre, @PageableDefault(size = 10) Pageable pageable) {
        return usuarioService.getUsuariosByTipoAndNombrePage("clientes", nombre, pageable);
    }

    @PostMapping
    public ResponseEntity<Usuario> createCliente(@RequestBody Usuario usuario) {
        usuario.setTipoUsuario("clientes");
        Usuario nuevoCliente = usuarioService.saveUsuario(usuario);
        nuevoCliente.setContraseña(null); // No devolver la contraseña
        return ResponseEntity.ok(nuevoCliente);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getClienteById(@PathVariable Long id) {
        Usuario cliente = usuarioService.getUsuarioById(id);
        if (cliente != null && "clientes".equals(cliente.getTipoUsuario())) {
            cliente.setContraseña(null); // No devolver la contraseña
            return ResponseEntity.ok(cliente);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCliente(@PathVariable Long id) {
        usuarioService.deleteUsuario(id);
        return ResponseEntity.ok().build();
    }
} 