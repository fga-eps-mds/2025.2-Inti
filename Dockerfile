# Build stage
FROM maven:3-eclipse-temurin-25 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Production stage
FROM openjdk:25 AS prod
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-Xmx2048M", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "/app.jar"]

# Development stage NOT WORKING YET (trying to mess with hot reload from spring-boot-devtools)
FROM maven:3-eclipse-temurin-25 AS dev
WORKDIR /app
RUN apt-get update && apt-get install -y inotify-tools
COPY . .
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

ENV SPRING_PROFILES_ACTIVE=dev

ENTRYPOINT ["/docker-entrypoint.sh"]