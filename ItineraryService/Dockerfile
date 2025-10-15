FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar todo el proyecto para resolver POM padre
COPY . .

# Descargar dependencias solo del módulo ItineraryService
RUN mvn -B dependency:go-offline -pl ItineraryService -am -Dmaven.repo.local=/root/.m2

# Compilar y generar el JAR (sin tests)
RUN mvn clean package -pl ItineraryService -am -DskipTests -Dmaven.repo.local=/root/.m2

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/ItineraryService/target/ItineraryService-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
