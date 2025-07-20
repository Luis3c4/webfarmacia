package com.proyecto.farmacia.webfarmacia.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

@RestController
@RequestMapping("/api/supabase")
@CrossOrigin(origins = "*")
public class SupabaseUploadController {

    @Value("${SUPABASE_URL}")
    private String supabaseUrl;

    @Value("${SUPABASE_SERVICE_ROLE_KEY}")
    private String serviceRoleKey;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadToSupabase(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("=== INICIO DE SUBIDA A SUPABASE ===");
            System.out.println("Supabase URL: " + supabaseUrl);
            System.out.println("Service Role Key (primeros 20 chars): " + (serviceRoleKey != null ? serviceRoleKey.substring(0, Math.min(20, serviceRoleKey.length())) + "..." : "NULL"));
            
            // Validar el archivo
            if (file.isEmpty()) {
                System.out.println("ERROR: El archivo está vacío");
                return ResponseEntity.badRequest().body("El archivo está vacío");
            }

            System.out.println("Archivo recibido: " + file.getOriginalFilename() + ", Tamaño: " + file.getSize() + " bytes");

            // Validar tipo de archivo
            String contentType = file.getContentType();
            System.out.println("Content-Type: " + contentType);
            
            if (contentType == null || !contentType.startsWith("image/")) {
                System.out.println("ERROR: Tipo de archivo no válido: " + contentType);
                return ResponseEntity.badRequest().body("Solo se permiten archivos de imagen. Tipo recibido: " + contentType);
            }

            // Generar nombre único para el archivo
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String filename = UUID.randomUUID().toString() + fileExtension;
            String filePath = "images/" + filename;
            
            System.out.println("Nombre del archivo generado: " + filename);
            System.out.println("Ruta del archivo: " + filePath);

            // Crear el cliente HTTP para Supabase
            RestTemplate restTemplate = new RestTemplate();
            
            // Configurar headers para Supabase Storage API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Bearer " + serviceRoleKey);
            headers.set("apikey", serviceRoleKey);
            
            System.out.println("Headers configurados: Authorization y apikey");

            // Crear el body de la petición
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return filename;
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // URL de Supabase Storage - API correcta
            String uploadUrl = supabaseUrl + "/storage/v1/object/images/" + filename;
            System.out.println("URL de subida: " + uploadUrl);

            System.out.println("Realizando petición a Supabase...");
            
            // Realizar la petición a Supabase
            ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
            );

            System.out.println("Respuesta de Supabase - Status: " + response.getStatusCode());
            System.out.println("Respuesta de Supabase - Body: " + response.getBody());

            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                // Construir la URL pública de la imagen
                String publicUrl = supabaseUrl + "/storage/v1/object/images/" + filename;
                System.out.println("URL pública generada: " + publicUrl);
                System.out.println("=== SUBIDA EXITOSA ===");
                return ResponseEntity.ok(publicUrl);
            } else {
                System.out.println("ERROR: Respuesta no exitosa de Supabase");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al subir la imagen a Supabase. Status: " + response.getStatusCode() + ", Response: " + response.getBody());
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("=== ERROR DE VALIDACIÓN EN SUBIDA A SUPABASE ===");
            System.err.println("Error de validación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error de validación: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("=== ERROR DE I/O EN SUBIDA A SUPABASE ===");
            System.err.println("Error de I/O: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error de I/O al procesar el archivo: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("=== ERROR EN SUBIDA A SUPABASE ===");
            System.err.println("Error al subir imagen a Supabase: " + e.getMessage());
            System.err.println("Tipo de excepción: " + e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor: " + e.getMessage() + " (Tipo: " + e.getClass().getSimpleName() + ")");
        }
    }

    // Método alternativo usando la API REST de Supabase
    @PostMapping("/upload-v2")
    public ResponseEntity<String> uploadToSupabaseV2(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("=== INICIO DE SUBIDA A SUPABASE V2 ===");
            
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("El archivo está vacío");
            }

            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String filename = UUID.randomUUID().toString() + fileExtension;
            
            System.out.println("Archivo: " + originalFilename + ", Tamaño: " + file.getSize() + " bytes");
            System.out.println("Nombre generado: " + filename);

            // Crear el cliente HTTP
            RestTemplate restTemplate = new RestTemplate();
            
            // Headers para Supabase
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + serviceRoleKey);
            headers.set("apikey", serviceRoleKey);
            headers.set("Content-Type", "application/octet-stream");
            headers.set("x-upsert", "true"); // Para sobrescribir si existe

            // Crear la entidad HTTP con los bytes del archivo
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

            // URL de la API de Supabase Storage
            String uploadUrl = supabaseUrl + "/storage/v1/object/images/" + filename;
            System.out.println("URL de subida V2: " + uploadUrl);

            // Realizar la petición
            ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
            );

            System.out.println("Respuesta V2 - Status: " + response.getStatusCode());
            System.out.println("Respuesta V2 - Body: " + response.getBody());

            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                String publicUrl = supabaseUrl + "/storage/v1/object/images/" + filename;
                System.out.println("=== SUBIDA V2 EXITOSA ===");
                return ResponseEntity.ok(publicUrl);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error V2: " + response.getStatusCode() + " - " + response.getBody());
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("=== ERROR DE VALIDACIÓN EN SUBIDA V2 ===");
            System.err.println("Error de validación V2: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error de validación V2: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("=== ERROR DE I/O EN SUBIDA V2 ===");
            System.err.println("Error de I/O V2: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error de I/O al procesar el archivo V2: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("=== ERROR EN SUBIDA V2 ===");
            System.err.println("Error V2: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error V2: " + e.getMessage());
        }
    }

    @GetMapping("/check-bucket")
    public ResponseEntity<String> checkBucket() {
        try {
            System.out.println("=== VERIFICANDO BUCKET ===");
            System.out.println("Supabase URL: " + supabaseUrl);
            System.out.println("Service Role Key (primeros 20 chars): " + (serviceRoleKey != null ? serviceRoleKey.substring(0, Math.min(20, serviceRoleKey.length())) + "..." : "NULL"));
            
            RestTemplate restTemplate = new RestTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + serviceRoleKey);
            headers.set("apikey", serviceRoleKey);

            // Listar todos los buckets
            String bucketsUrl = supabaseUrl + "/storage/v1/bucket";
            System.out.println("Listando buckets: " + bucketsUrl);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                bucketsUrl,
                HttpMethod.GET,
                requestEntity,
                String.class
            );

            System.out.println("Respuesta de buckets - Status: " + response.getStatusCode());
            System.out.println("Respuesta de buckets - Body: " + response.getBody());

            if (response.getStatusCode() == HttpStatus.OK) {
                return ResponseEntity.ok("Buckets disponibles: " + response.getBody());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al listar buckets. Status: " + response.getStatusCode() + ", Response: " + response.getBody());
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("Error de validación al verificar buckets: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error de validación al verificar buckets: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Error al verificar buckets: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al verificar buckets: " + e.getMessage());
        }
    }

    @PostMapping("/create-bucket")
    public ResponseEntity<String> createBucket() {
        try {
            System.out.println("=== CREANDO BUCKET ===");
            
            RestTemplate restTemplate = new RestTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + serviceRoleKey);
            headers.set("apikey", serviceRoleKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Crear el bucket
            String bucketUrl = supabaseUrl + "/storage/v1/bucket";
            String bucketConfig = "{\"id\":\"images\",\"name\":\"images\",\"public\":true}";
            
            System.out.println("Creando bucket: " + bucketUrl);
            System.out.println("Configuración: " + bucketConfig);

            HttpEntity<String> requestEntity = new HttpEntity<>(bucketConfig, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                bucketUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
            );

            System.out.println("Respuesta de creación - Status: " + response.getStatusCode());
            System.out.println("Respuesta de creación - Body: " + response.getBody());

            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                return ResponseEntity.ok("Bucket 'images' creado exitosamente");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear bucket. Status: " + response.getStatusCode() + ", Response: " + response.getBody());
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("Error de validación al crear bucket: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error de validación al crear bucket: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Error al crear bucket: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al crear bucket: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{filename}")
    public ResponseEntity<String> deleteFromSupabase(@PathVariable String filename) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + serviceRoleKey);
            headers.set("apikey", serviceRoleKey);

            String filePath = "images/" + filename;
            String deleteUrl = supabaseUrl + "/storage/v1/object/" + filePath;

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                deleteUrl,
                HttpMethod.DELETE,
                requestEntity,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.NO_CONTENT) {
                return ResponseEntity.ok("Imagen eliminada exitosamente");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar la imagen: " + response.getStatusCode());
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("Error de validación al eliminar imagen de Supabase: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error de validación al eliminar imagen: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Error al eliminar imagen de Supabase: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor: " + e.getMessage());
        }
    }
} 