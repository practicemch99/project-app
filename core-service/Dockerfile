FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY build/libs/core-service.jar app.jar
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
