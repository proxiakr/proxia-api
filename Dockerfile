FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./gradlew :core:core-api:bootJar -x test -x contextTest

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/core/core-api/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
