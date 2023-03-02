FROM openjdk:17-jdk-slim-buster

WORKDIR /app
COPY ./target/iam-0.0.1-SNAPSHOT.jar /app

EXPOSE 8080

CMD ["java", "-jar", "iam-0.0.1-SNAPSHOT.jar"]