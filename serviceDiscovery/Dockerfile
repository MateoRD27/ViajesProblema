FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar TODO el proyecto para que Maven pueda resolver el POM padre
COPY . .

# Construir solo el módulo serviceDiscovery y sus dependencias
RUN mvn clean package -pl serviceDiscovery -am -DskipTests -Dmaven.repo.local=/root/.m2

# Imagen final
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/serviceDiscovery/target/serviceDiscovery-*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]

