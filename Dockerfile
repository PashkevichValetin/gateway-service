# Build stage
FROM gradle:8.7-jdk21-alpine AS build
WORKDIR /app

# Копируем файлы для зависимостей
COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon

# Копируем исходный код и собираем
COPY src ./src
RUN gradle clean bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21.0.4_7-jre-alpine

# Устанавливаем зависимости
RUN apk upgrade --no-cache && \
    apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/Europe/Moscow /etc/localtime && \
    echo "Europe/Moscow" > /etc/timezone

# Создаем пользователя
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Копируем jar
COPY --from=build --chown=appuser:appgroup /app/build/libs/gateway.jar app.jar

# Создаем директорию для логов
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app/logs

USER appuser

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

# Запуск с оптимизациями
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-Duser.timezone=Europe/Moscow", \
    "-jar", "/app/app.jar"]