package com.proyecto.farmacia.webfarmacia.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class FileUploadController {

    private static final String UPLOAD_DIR = "src/main/resources/static/imagenes/productos/";

    @PostMapping("/image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // Crear el directorio si no existe
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Validar el tipo de archivo
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("Solo se permiten archivos de imagen");
            }

            // Generar nombre único para el archivo
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String filename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = Paths.get(UPLOAD_DIR + filename);

            // Guardar el archivo
            Files.write(filePath, file.getBytes());

            // Retornar la URL relativa del archivo
            String imageUrl = "/imagenes/productos/" + filename;
            
            System.out.println("Imagen subida exitosamente: " + imageUrl);
            
            return ResponseEntity.ok(imageUrl);

        } catch (IOException e) {
            System.err.println("Error al subir imagen: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al subir la imagen: " + e.getMessage());
        }
    }

    @DeleteMapping("/image")
    public ResponseEntity<String> deleteImage(@RequestParam("url") String imageUrl) {
        try {
            // Extraer el nombre del archivo de la URL
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(UPLOAD_DIR + filename);

            // Verificar si el archivo existe
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            // Eliminar el archivo
            Files.delete(filePath);
            
            System.out.println("Imagen eliminada exitosamente: " + filename);
            
            return ResponseEntity.ok("Imagen eliminada exitosamente");

        } catch (IOException e) {
            System.err.println("Error al eliminar imagen: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al eliminar la imagen: " + e.getMessage());
        }
    }

    @GetMapping("/images")
    public ResponseEntity<String[]> listImages() {
        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                return ResponseEntity.ok(new String[0]);
            }

            File[] files = uploadDir.listFiles((dir, name) -> 
                name.toLowerCase().endsWith(".jpg") || 
                name.toLowerCase().endsWith(".jpeg") || 
                name.toLowerCase().endsWith(".png") || 
                name.toLowerCase().endsWith(".gif") ||
                name.toLowerCase().endsWith(".webp")
            );

            if (files == null) {
                return ResponseEntity.ok(new String[0]);
            }

            String[] imageUrls = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                imageUrls[i] = "/imagenes/productos/" + files[i].getName();
            }

            return ResponseEntity.ok(imageUrls);

        } catch (Exception e) {
            System.err.println("Error al listar imágenes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
} 