FROM eclipse-temurin:22-jdk
LABEL authors="Vitalik Savchuk"
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]