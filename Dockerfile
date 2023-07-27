# Build stage
FROM maven:3.8.5-openjdk-17-slim@sha256:cebb9c297b3607235bb53327f916b4213f0ee311b5b02b7ffe408c4be0f99c60 AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package -Dmaven.test.skip

# Package stage
FROM openjdk:17-jdk-slim-buster@sha256:4a6cf1a4bba19ac9c4906ab35221b5cd798327ad6892f568c7d58ac8845ea5fa
COPY --from=build /home/app/target/iam-0.0.1-SNAPSHOT.jar /usr/local/lib/iam.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/local/lib/iam.jar"]
