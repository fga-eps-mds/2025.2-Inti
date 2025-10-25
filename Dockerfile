
FROM maven:3-eclipse-temurin-25 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests -Pprod  # ← ADD -Pprod AQUI!

# Production stage  
FROM openjdk:25
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Criar usuário não-root para segurança
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

ENTRYPOINT ["java", "-Xmx2048M", "-jar", "app.jar"]