# Use an official Maven image to build the app
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the application source code
COPY . .

# Build the application
RUN mvn clean package -DskipTests

# Use a lightweight JRE image to run the app
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy jar from build image
COPY --from=build /app/target/contentGenerater-0.0.1-SNAPSHOT.jar app.jar

# Expose port (adjust if your app runs on a different port)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
