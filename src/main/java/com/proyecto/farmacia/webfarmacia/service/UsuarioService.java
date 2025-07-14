package com.proyecto.farmacia.webfarmacia.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import com.proyecto.farmacia.webfarmacia.model.Usuario;
import com.proyecto.farmacia.webfarmacia.repository.UsuarioRepository;

@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon.key}")
    private String supabaseAnonKey;

    public Usuario saveUsuario(Usuario usuario) {
        try {
            System.out.println("=== GUARDANDO USUARIO ===");
            System.out.println("ID: " + usuario.getIdUsuario());
            System.out.println("Nombre: " + usuario.getNombre());
            System.out.println("Email: " + usuario.getEmail());
            System.out.println("Tipo Usuario: " + usuario.getTipoUsuario());
            System.out.println("Teléfono: " + usuario.getTelefono());
            System.out.println("Activo: " + usuario.getActivo());
            
            // Establecer valores por defecto si no están definidos
            if (usuario.getActivo() == null) {
                usuario.setActivo(true);
                System.out.println("Activo establecido por defecto: true");
            }
            if (usuario.getFechaCreacion() == null) {
                usuario.setFechaCreacion(LocalDateTime.now());
                System.out.println("Fecha Creación establecida por defecto: " + usuario.getFechaCreacion());
            }
            if (usuario.getCreatedAt() == null) {
                usuario.setCreatedAt(LocalDateTime.now());
                System.out.println("Created At establecido por defecto: " + usuario.getCreatedAt());
            }
            if (usuario.getUpdatedAt() == null) {
                usuario.setUpdatedAt(LocalDateTime.now());
                System.out.println("Updated At establecido por defecto: " + usuario.getUpdatedAt());
            }
            
            System.out.println("Intentando guardar usuario...");
            Usuario usuarioGuardado = usuarioRepository.save(usuario);
            System.out.println("Usuario guardado exitosamente con ID: " + usuarioGuardado.getIdUsuario());
            return usuarioGuardado;
            
        } catch (Exception e) {
            System.err.println("=== ERROR AL GUARDAR USUARIO ===");
            System.err.println("Error: " + e.getMessage());
            System.err.println("Tipo de error: " + e.getClass().getSimpleName());
            e.printStackTrace();
            throw e;
        }
    }

    public Optional<Usuario> findByEmailAndContraseña(String email, String contraseña) {
        System.out.println("=== INTENTO DE LOGIN ===");
        System.out.println("Email recibido: '" + email + "'");
        System.out.println("Contraseña recibida: '" + contraseña + "'");
        
        // Obtener todos los usuarios para debug
        List<Usuario> todosUsuarios = usuarioRepository.findAll();
        System.out.println("Total de usuarios en BD: " + todosUsuarios.size());
        
        todosUsuarios.forEach(u -> {
            System.out.println("Usuario en BD - ID: " + u.getIdUsuario() + 
                             ", Email: '" + u.getEmail() + "'" +
                             ", Contraseña: '" + u.getContraseña() + "'" +
                             ", Tipo: " + u.getTipoUsuario() + 
                             ", Nombre: " + u.getNombre());
        });
        
        Optional<Usuario> usuarioEncontrado = todosUsuarios.stream()
            .filter(u -> {
                String emailBD = u.getEmail() != null ? u.getEmail().trim().toLowerCase() : "";
                String emailInput = email != null ? email.trim().toLowerCase() : "";
                String passBD = u.getContraseña() != null ? u.getContraseña().trim() : "";
                String passInput = contraseña != null ? contraseña.trim() : "";
                boolean emailCoincide = emailBD.equals(emailInput);
                boolean contraseñaCoincide = passBD.equals(passInput);
                System.out.println("Comparando: '" + emailBD + "' == '" + emailInput + "' => " + emailCoincide);
                System.out.println("Comparando: '" + passBD + "' == '" + passInput + "' => " + contraseñaCoincide);
                return emailCoincide && contraseñaCoincide;
            })
            .findFirst();
        
        if (usuarioEncontrado.isPresent()) {
            System.out.println("Usuario encontrado: " + usuarioEncontrado.get().getNombre());
        } else {
            System.out.println("No se encontró usuario con esas credenciales");
        }
        
        System.out.println("=========================");
        return usuarioEncontrado;
    }
    
    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }
    
    public List<Usuario> getUsuariosByTipo(String tipoUsuario) {
        return usuarioRepository.findAll().stream()
            .filter(u -> tipoUsuario.equals(u.getTipoUsuario()))
            .toList();
    }
    
    public Page<Usuario> getUsuariosByTipoPage(String tipoUsuario, Pageable pageable) {
        List<Usuario> usuariosFiltrados = getUsuariosByTipo(tipoUsuario);
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), usuariosFiltrados.size());
        
        if (start > usuariosFiltrados.size()) {
            start = usuariosFiltrados.size();
        }
        
        List<Usuario> pageContent = usuariosFiltrados.subList(start, end);
        
        return new org.springframework.data.domain.PageImpl<>(
            pageContent, 
            pageable, 
            usuariosFiltrados.size()
        );
    }
    
    public Page<Usuario> getUsuariosByTipoAndNombrePage(String tipoUsuario, String nombre, Pageable pageable) {
        List<Usuario> usuariosFiltrados = usuarioRepository.findAll().stream()
            .filter(u -> tipoUsuario.equals(u.getTipoUsuario()))
            .filter(u -> u.getNombre() != null && u.getNombre().toLowerCase().contains(nombre.toLowerCase()))
            .toList();
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), usuariosFiltrados.size());
        
        if (start > usuariosFiltrados.size()) {
            start = usuariosFiltrados.size();
        }
        
        List<Usuario> pageContent = usuariosFiltrados.subList(start, end);
        
        return new org.springframework.data.domain.PageImpl<>(
            pageContent, 
            pageable, 
            usuariosFiltrados.size()
        );
    }
    
    public Usuario getUsuarioById(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }
    
    public void deleteUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
    
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findAll().stream()
            .filter(u -> u.getEmail() != null && u.getEmail().equals(email))
            .findFirst();
    }

    public Optional<Usuario> findBySupabaseId(String supabaseId) {
        return usuarioRepository.findAll().stream()
            .filter(u -> u.getSupabaseId() != null && u.getSupabaseId().equals(supabaseId))
            .findFirst();
    }

    public Optional<Usuario> loginConSupabase(String email, String contraseña) {
        System.out.println("=== LOGIN CON SUPABASE AUTH ===");
        System.out.println("Email recibido: '" + email + "'");
        System.out.println("Contraseña recibida: '" + contraseña + "'");
        try {
            RestTemplate restTemplate = new RestTemplate();
            String loginUrl = supabaseUrl + "/auth/v1/token?grant_type=password";
            System.out.println("URL de login Supabase: " + loginUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", supabaseAnonKey);

            HashMap<String, String> body = new HashMap<>();
            body.put("email", email);
            body.put("password", contraseña);

            HttpEntity<HashMap<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(loginUrl, HttpMethod.POST, request, String.class);
            System.out.println("Respuesta Supabase: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody());

            if (response.getStatusCode() == HttpStatus.OK) {
                // Login válido en Supabase, buscar usuario en base local
                return findByEmail(email);
            } else {
                System.out.println("Login inválido en Supabase");
                return Optional.empty();
            }
        } catch (Exception e) {
            System.err.println("Error al validar con Supabase Auth: " + e.getMessage());
            return Optional.empty();
        }
    }
} 