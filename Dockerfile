# Stage 1: Build the application using Gradle with JDK 25
FROM gradle:jdk25 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon -x test

# Stage 2: Run the application using a lightweight JDK 25 JRE
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

# Render only supports a single public port, which must match what your app binds to
EXPOSE 25565

# Fix: Use individual string arguments so the shell parses flags correctly
ENTRYPOINT ["java", "-jar", "app.jar", "--server", "--port", "25565"]
