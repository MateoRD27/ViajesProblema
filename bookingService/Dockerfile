FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar TODO el proyecto para que Maven pueda resolver el POM padre
COPY . .

# Descargar dependencias solo del módulo bookingService (aprovecha cache)
RUN mvn -B dependency:go-offline -pl bookingService -am -Dmaven.repo.local=/root/.m2

# Compilar y generar el JAR (sin tests)
RUN mvn clean package -pl bookingService -am -DskipTests -Dmaven.repo.local=/root/.m2

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/bookingService/target/bookingService-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
