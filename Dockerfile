# Use the official OpenJDK 21 image
FROM eclipse-temurin:21-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file into the container
COPY target/*.jar app.jar

# Command to run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
