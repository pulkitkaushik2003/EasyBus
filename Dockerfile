# Use Maven with JDK 21 (LTS) for build
FROM maven:3.9.9-eclipse-temurin-21 AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Use Eclipse Temurin JDK 21 (LTS) for runtime
FROM eclipse-temurin:21-jdk

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/ticket-0.0.1-SNAPSHOT.jar ticket.jar

# Expose the port the app runs on
EXPOSE 2626

# Run the application
ENTRYPOINT ["java", "-jar", "ticket.jar"]
