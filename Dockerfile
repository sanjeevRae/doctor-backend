# Use an official OpenJDK runtime as a parent image
FROM maven:3.8.4-openjdk-17 AS build

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Build the project
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy compiled jar from the builder
COPY --from=build /app/target/doctor-saab-1.0-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]
