FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY order-service/pom.xml order-service/pom.xml
COPY catalog-service/pom.xml catalog-service/pom.xml
COPY assignment-service/pom.xml assignment-service/pom.xml
COPY tracking-service/pom.xml tracking-service/pom.xml

RUN mvn -pl assignment-service -am dependency:go-offline

COPY assignment-service/src assignment-service/src

RUN mvn -pl assignment-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/assignment-service/target/assignment-service-*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]