## Быстрый старт (Docker DB + приложение)

### Требования
- Docker + Docker Compose
- Java 17
- Maven

### 1) Поднять БД (PostgreSQL) в Docker
```zsh
cd <папка-проекта>
docker-compose up -d
docker ps
```

БД будет доступна на хосте по адресу: `localhost:5433` (проброс порта из контейнера).

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

### 3) Остановить
```zsh
cd <папка-проекта>
docker-compose down
```

Полностью удалить данные БД (осторожно):
```zsh
docker-compose down -v
```
