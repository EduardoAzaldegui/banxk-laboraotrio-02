# Usa un argumento para definir la versión de Java por default si no se pasa
ARG JAVA_VERSION=17

# Usa la imagen oficial de OpenJDK
FROM openjdk:25-ea-${JAVA_VERSION}-slim-bullseye


# Instala curl para healthcheck y diagnósticos
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# Puerto en el que escucha tu app
EXPOSE 8082

# Directorio de trabajo
WORKDIR /app

# Crea la carpeta de logs (como root)
RUN mkdir -p /app/logs

# Copia el JAR construido
COPY target/*.jar app.jar

# Ejecuta la app como root (¡sí, root!)
USER root

# Comando para ejecutar la app
ENTRYPOINT ["java", "-jar", "app.jar"]