## Быстрый старт (Docker DB + Kafka + приложение)

### Требования
- Docker + Docker Compose
- Java 21
- Maven

### 1) Поднять PostgreSQL + Kafka в Docker
```zsh
cd <папка-проекта>
docker compose up -d
docker ps
```

PostgreSQL будет доступен на хосте по адресу: `localhost:5433`.
Kafka будет доступна на хосте по адресу: `localhost:9092`.

Проверка, что БД отвечает:
```zsh
docker exec -i portal_db psql -U myuser -d myproject -c "SELECT 1;"
```

### 2) Собрать и запустить приложение
Сборка:
```zsh
cd <папка-проекта>
mvn -DskipTests clean package
```

Если порт `8080` занят:
```zsh
lsof -i :8080
kill -9 <PID>
```

Запуск:
```zsh
cd <папка-проекта>
java -jar target/portal-1.0.0.war
```

Если нужно запустить на другом порту (например, 8081):
```zsh
java -jar target/portal-1.0.0.war --server.port=8081
```

Проверка:
```zsh
curl -I http://localhost:8080/ | cat
```

Открыть в браузере: http://localhost:8080/

### 3) Проверка Kafka (тестовый эндпоинт)
```zsh
curl -X POST http://localhost:8080/api/kafka/test \
  -H 'Content-Type: application/json' \
  -d '{"key":"demo","payload":"hello"}'
```

### 4) Логи ошибок
Файл с ошибками и контекстом: `logs/portal-error.log`.

### 5) Остановить
```zsh
cd <папка-проекта>
docker compose down
```

Полностью удалить данные БД (осторожно):
```zsh
docker compose down -v
```
