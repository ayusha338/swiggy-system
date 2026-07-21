# Stage 1: Build - poora multi-module project ka context chahiye
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Pehle sirf pom files copy karo (dependency caching ke liye) -
# poora multi-module structure yahan zaroori hai taaki Maven
# parent-child relationship resolve kar sake
COPY pom.xml .
COPY order-service/pom.xml order-service/pom.xml
COPY catalog-service/pom.xml catalog-service/pom.xml
COPY assignment-service/pom.xml assignment-service/pom.xml
COPY tracking-service/pom.xml tracking-service/pom.xml

# Sirf order-service ke liye dependencies download karo (offline mode ke bina)
RUN mvn -pl order-service -am dependency:go-offline

# Ab actual source code copy karo
COPY order-service/src order-service/src

# Sirf order-service module build karo (-pl), uske dependencies bhi (-am = also make)
RUN mvn -pl order-service -am clean package -DskipTests

# Stage 2: Runtime - halka image, sirf JRE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/order-service/target/order-service-*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]