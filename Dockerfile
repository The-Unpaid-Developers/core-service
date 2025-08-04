FROM docker.io/maven:3.9-eclipse-temurin-21-alpine AS build
COPY src /usr/app/src
COPY pom.xml /usr/app
RUN mvn -f /usr/app/pom.xml clean install -Dmaven.test.skip=true

# Package stage
FROM docker.io/eclipse-temurin:21-jre-alpine
COPY --from=build /usr/app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]