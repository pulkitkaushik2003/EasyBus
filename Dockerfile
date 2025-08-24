# Use Maven with JDK 21 (closest LTS) to build
# JDK 22 builds bhi Maven 3.9+ pe chalega
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Use Eclipse Temurin JDK 22 for runtime
FROM eclipse-temurin:22-jdk

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Render sets PORT dynamically, don't hardcode
EXPOSE 2626

ENTRYPOINT ["java", "-jar", "app.jar"]
