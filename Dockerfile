FROM openjdk:21-jdk-slim

WORKDIR /app

COPY build/libs/quant-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8098

CMD ["java", "-jar", "app.jar"]