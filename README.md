# WebFarmacia

Sistema de gestión de farmacia web desarrollado con Spring Boot, Thymeleaf y PostgreSQL.

## Características

- 🏥 Gestión de productos farmacéuticos
- 👥 Gestión de clientes
- 💰 Sistema de ventas
- 🛒 Carrito de compras
- 💳 Integración con PayPal
- 📊 Dashboard administrativo
- 🖼️ Gestión de imágenes con Supabase Storage

## Tecnologías Utilizadas

- **Backend**: Spring Boot 3.5.3, Java 21
- **Frontend**: Thymeleaf, HTML5, CSS3, JavaScript
- **Base de Datos**: PostgreSQL (Supabase)
- **Pagos**: PayPal SDK
- **Almacenamiento**: Supabase Storage
- **Build Tool**: Maven

## Configuración Inicial

### Prerrequisitos

- Java 21 o superior
- Maven 3.6 o superior
- Cuenta en Supabase
- Cuenta de desarrollador en PayPal

### Configuración de Variables de Entorno

1. **Ejecuta el script de configuración** (Windows):
   ```powershell
   .\setup-env.ps1
   ```

   O en Linux/Mac:
   ```bash
   source setup-env.sh
   ```

2. **Configuración manual** (si prefieres hacerlo manualmente):
   
   Consulta el archivo `ENVIRONMENT_SETUP.md` para las instrucciones detalladas.

### Ejecutar la Aplicación

**Opción 1: Script automático (Recomendado)**
```powershell
.\run-app.ps1
```

**Opción 2: Manual**
```bash
# Configurar variables de entorno
.\setup-env.ps1

# Ejecutar la aplicación
.\mvnw.cmd spring-boot:run
```

La aplicación estará disponible en: http://localhost:8080

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/proyecto/farmacia/webfarmacia/
│   │   ├── config/          # Configuraciones (PayPal, Jackson)
│   │   ├── controller/      # Controladores REST
│   │   ├── model/          # Entidades JPA
│   │   ├── repository/     # Repositorios de datos
│   │   ├── service/        # Lógica de negocio
│   │   └── paypal/         # Integración con PayPal
│   └── resources/
│       ├── static/         # Archivos estáticos (CSS, JS, imágenes)
│       └── templates/      # Plantillas Thymeleaf
```

## Funcionalidades Implementadas

### Procesamiento Automático de Ventas
- ✅ Descuento automático del stock de productos al procesar un pago exitoso
- ✅ Registro automático de ventas en la base de datos
- ✅ Creación automática de clientes si no existen
- ✅ Actualización del estado de stock (IN_STOCK, LOW_STOCK, OUT_OF_STOCK)
- ✅ Integración completa con PayPal

### Flujo de Compra
1. Usuario agrega productos al carrito
2. Completa formulario de envío en la pasarela
3. Realiza pago con PayPal
4. Al confirmar el pago exitoso:
   - Se descuenta automáticamente el stock
   - Se registra la venta en la base de datos
   - Se crea/actualiza el cliente
   - Se muestran los detalles de la compra

## Problemas Comunes y Soluciones

### Error: "Method parameter 'id': Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'"

**Causa**: Conflicto de rutas entre controladores.

**Solución**: Ya corregido en este proyecto. El `ProductoVistaController` ahora usa la ruta `/producto/detalle/id/{id}` en lugar de `/producto/detalle/{id}`.

### Error: "The declared package does not match the expected package"

**Causa**: Inconsistencia en el nombre del paquete.

**Solución**: Ya corregido. El paquete ahora es `com.proyecto.farmacia.webfarmacia.config` (con c minúscula).

### Error: "Port 8080 was already in use"

**Causa**: Otra aplicación está usando el puerto 8080.

**Solución**: El script `run-app.ps1` detecta y detiene automáticamente procesos en el puerto 8080.

## Seguridad

⚠️ **IMPORTANTE**: Las credenciales en este proyecto están expuestas para fines de desarrollo. En producción:

1. Usa variables de entorno del sistema
2. Implementa un gestor de secretos
3. Nunca commitear credenciales reales
4. Rota las credenciales regularmente

## Contribución

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles. 
