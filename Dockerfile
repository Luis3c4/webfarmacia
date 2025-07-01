# Usa una imagen oficial de Java y Maven
FROM eclipse-temurin:21-jdk

# Crea carpeta para el proyecto
WORKDIR /app

# Copia solo los archivos de Maven y descarga dependencias primero (mejor caché)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN ./mvnw dependency:go-offline

# Ahora copia el resto del código
COPY . .

RUN java -version
RUN ./mvnw -version

# Construye el proyecto
RUN ./mvnw clean package -DskipTests

# Expón el puerto (ajústalo si tu app escucha en otro)
EXPOSE 8080

# Comando para ejecutar tu app
CMD ["java", "-jar", "target/webfarmacia-0.0.1-SNAPSHOT.jar"]
