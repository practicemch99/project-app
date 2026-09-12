FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY build/libs/user-service.jar app.jar
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
