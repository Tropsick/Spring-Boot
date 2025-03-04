# Используем образ с Java 17
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файлы проекта в контейнер
COPY . /app

# Загружаем зависимости
RUN ./mvnw clean install -X


# Указываем команду для запуска приложения
CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]
