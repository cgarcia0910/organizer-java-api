
# =========================================
# ETAPA 1: BUILD
# =========================================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copiamos primero las dependencias
COPY pom.xml .

# Descargamos dependencias Maven
RUN mvn dependency:go-offline

# Copiamos el proyecto completo
COPY src ./src

# Compilamos y generamos el JAR
# Incluye OpenAPI Generator
RUN mvn clean package -DskipTests


# =========================================
# ETAPA 2: RUNTIME
# =========================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Usuario sin privilegios
RUN useradd -r -u 10001 spring

# Copiamos el JAR generado
COPY --from=build /app/target/*.jar app.jar

# Permisos
RUN chown spring:spring app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
