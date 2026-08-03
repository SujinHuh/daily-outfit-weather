FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY src ./src

RUN ./gradlew bootJar --no-daemon --rerun-tasks

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S app \
	&& adduser -S app -G app \
	&& mkdir -p /app/logs \
	&& chown -R app:app /app/logs

COPY --from=build /workspace/build/libs/*.jar app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
