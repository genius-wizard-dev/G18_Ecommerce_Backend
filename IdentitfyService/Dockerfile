# Start with a Maven image that includes JDK 21
FROM maven:3.9.9-amazoncorretto-21 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build source code with Maven
RUN mvn package -DskipTests

# Start with Amazon Corretto JDK 21
FROM amazoncorretto:21.0.4

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Chờ Kafka khởi động trước khi chạy ứng dụng
ENTRYPOINT ["sh", "-c", "sleep 10 && java -jar app.jar"]
