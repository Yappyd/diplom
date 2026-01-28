#BUILD STAGE
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN ./mvnw dependency:go-offline

COPY src src

RUN ./mvnw package -DskipTests



#RUNTIME STAGE
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/target/auth-service-*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "--enable-preview", "-jar", "app.jar"]