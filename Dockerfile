# Use the official Maven image to build the application
FROM maven:3.8.6-openjdk-22 AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Use the official OpenJDK image to run the application
FROM openjdk:22-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/ticket-0.0.1-SNAPSHOT.jar ticket.jar

# Expose the port the app runs on
EXPOSE 2626

# Run the application
ENTRYPOINT ["java", "-jar", "ticket.jar"]
