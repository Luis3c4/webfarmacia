package com.proyecto.farmacia.webfarmacia.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.farmacia.webfarmacia.model.Usuario;
import com.proyecto.farmacia.webfarmacia.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<Usuario>> getAllUsuarios() {
        List<Usuario> usuarios = usuarioService.getAllUsuarios();
        // No devolver contraseñas por seguridad
        usuarios.forEach(u -> u.setContraseña(null));
        return ResponseEntity.ok(usuarios);
    }

    @PostMapping
    public ResponseEntity<Usuario> createUsuario(@RequestBody Usuario usuario) {
        System.out.println("=== CREANDO USUARIO ===");
        System.out.println("Email: " + usuario.getEmail());
        System.out.println("Supabase ID: " + usuario.getSupabaseId());
        System.out.println("Nombre: " + usuario.getNombre());
        System.out.println("Tipo Usuario: " + usuario.getTipoUsuario());
        
        Usuario nuevoUsuario = usuarioService.saveUsuario(usuario);
        nuevoUsuario.setContraseña(null); // No devolver la contraseña
        return ResponseEntity.ok(nuevoUsuario);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUsuario(@RequestBody Usuario loginRequest) {
        System.out.println("=== LOGIN REQUEST ===");
        System.out.println("Email: " + loginRequest.getEmail());
        System.out.println("Contraseña: " + loginRequest.getContraseña());
        
        if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "El correo es requerido");
            return ResponseEntity.badRequest().body(error);
        }
        
        if (loginRequest.getContraseña() == null || loginRequest.getContraseña().trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "La contraseña es requerida");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Validar contra base de datos local
        return usuarioService.findByEmailAndContraseña(loginRequest.getEmail(), loginRequest.getContraseña())
            .map(usuario -> {
                System.out.println("Login exitoso para: " + usuario.getNombre());
                
                // Crear respuesta con los datos necesarios para el frontend
                Map<String, Object> response = new HashMap<>();
                response.put("idUsuario", usuario.getIdUsuario());
                response.put("nombre", usuario.getNombre());
                response.put("email", usuario.getEmail());
                response.put("tipoUsuario", usuario.getTipoUsuario());
                response.put("telefono", usuario.getTelefono());
                
                return ResponseEntity.ok((Object) response);
            })
            .orElseGet(() -> {
                System.out.println("Login fallido - credenciales incorrectas");
                Map<String, String> error = new HashMap<>();
                error.put("error", "Usuario o contraseña incorrectos");
                return ResponseEntity.status(401).body(error);
            });
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Usuario> usuarios = usuarioService.getAllUsuarios();
            response.put("status", "success");
            response.put("message", "Conexión exitosa a la base de datos");
            response.put("total_usuarios", usuarios.size());
            response.put("usuarios", usuarios.stream().map(u -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", u.getIdUsuario());
                userMap.put("email", u.getEmail());
                userMap.put("nombre", u.getNombre());
                userMap.put("tipo_usuario", u.getTipoUsuario());
                return userMap;
            }).toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error al conectar con la base de datos: " + e.getMessage());
            response.put("error_type", e.getClass().getSimpleName());
            e.printStackTrace();
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/test-connection")
    public ResponseEntity<Map<String, Object>> testSimpleConnection() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("status", "success");
            response.put("message", "Controlador funcionando correctamente");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error en el controlador: " + e.getMessage());
            response.put("error_type", e.getClass().getSimpleName());
            e.printStackTrace();
            return ResponseEntity.status(500).body(response);
        }
    }
} 