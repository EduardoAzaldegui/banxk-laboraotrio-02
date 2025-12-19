# ETAPA 1: Construcción (Build)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos primero los archivos de definición para aprovechar el caché de capas de Docker
COPY pom.xml .
COPY src ./src

# Ejecutamos el empaquetado (saltando tests para agilizar el build del contenedor)
# Esto generará también las fuentes Avro
RUN mvn clean package -DskipTests

# ETAPA 2: Ejecución (Runtime)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiamos el JAR generado en la etapa anterior
# Ajustamos el nombre para que coincida con el artifactId del pom.xml
COPY --from=build /app/target/card-ops-producer-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto definido en application.yml [cite: 887]
EXPOSE 8082

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]