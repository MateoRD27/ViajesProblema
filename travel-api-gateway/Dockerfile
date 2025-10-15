FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar todo el proyecto
COPY . .

# Descargar dependencias solo del módulo travel-api-gateway
RUN mvn -B dependency:go-offline -pl travel-api-gateway -am -Dmaven.repo.local=/root/.m2

# Compilar y generar el JAR (sin tests)
RUN mvn clean package -pl travel-api-gateway -am -DskipTests -Dmaven.repo.local=/root/.m2

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/travel-api-gateway/target/travel-api-gateway-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
