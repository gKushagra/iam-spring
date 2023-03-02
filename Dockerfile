# Build stage
FROM maven:3.8.5-openjdk-17-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package -Dmaven.test.skip

# Package stage
FROM openjdk:17-jdk-slim-buster
COPY --from=build /home/app/target/iam-0.0.1-SNAPSHOT.jar /usr/local/lib/iam.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/local/lib/iam.jar"]
