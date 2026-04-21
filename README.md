# Portal — цифровая экосистема МФТИ

Продолжение проекта реализуется здесь: https://github.com/korsixe/Portal2

**Telegram-бот:** https://github.com/korsixe/Portal.Bot/

Portal — единая цифровая экосистема для решения ключевых бытовых проблем студентов МФТИ. Проект содержит один функциональный блок:

**Маркетплейс** — безопасная платформа для покупки и продажи б/у вещей исключительно среди студентов.

Проект реализуется в рамках программы "Высшая школа программной инженерии" МФТИ при поддержке компании ООО MWS.

---

## Архитектура

Проект построен по классической трёхзвенной архитектуре:

- **Презентационный слой:** React (SPA, порт 3000) + JSP-страницы
- **Бизнес-логика:** Spring Boot 3, REST API (порт 8080)
- **Слой данных:** PostgreSQL, управляемый через Spring Data JPA

---

## Технологический стек

| Слой | Технологии |
|---|---|
| Backend | Java 21, Spring Boot 3, Spring Security, Spring Data JPA |
| Frontend | React 18, React Router v6 |
| База данных | PostgreSQL 15 |
| Очередь сообщений | Apache Kafka + Zookeeper |
| Поиск | Elasticsearch 8 |
| Email-уведомления | Gmail SMTP / Mailtrap |
| Сборка | Maven |
| Контейнеризация | Docker, Docker Compose |
| VCS | Git |

---

## Конфигурация

### docker-compose.yml

```yaml
services:
  db:
    image: postgres:15-alpine
    container_name: portal_db
    environment:
      POSTGRES_DB: myproject
      POSTGRES_USER: myuser
      POSTGRES_PASSWORD: mypassword
    ports:
      - "5433:5432"
    volumes:
      - db_data:/var/lib/postgresql/data
    restart: always

  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    container_name: portal_zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    container_name: portal_kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.10.2
    container_name: portal_es
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"
    restart: always

volumes:
  db_data:
```

### Подключение к БД

- **URL:** `jdbc:postgresql://localhost:5433/myproject`
- **Пользователь:** `myuser`
- **Пароль:** `mypassword`
- **Драйвер:** `org.postgresql.Driver`

---

## Предварительные требования

- Java Development Kit (JDK) 21+
- Apache Maven 3.6+
- Docker и Docker Compose
- Node.js 18+ и npm
- Git
- Рекомендуемая ОС: Linux, macOS

---

## Запуск проекта

### 1. Клонирование репозитория

```bash
git clone https://github.com/korsixe/Portal2
cd Portal2
```

### 2. Запуск инфраструктуры

```bash
docker-compose up -d
```

Поднимает PostgreSQL, Kafka, Zookeeper, Elasticsearch.

### 3. Настройка переменных окружения

Создай файл `.env.local` в корне проекта:

```
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

### 4. Запуск бэкенда

```bash
mvn spring-boot:run
```

Бэкенд будет доступен на `http://localhost:8080`.

### 5. Запуск фронтенда

```bash
cd src/main/frontend
npm install
npm start
```

Фронтенд будет доступен на `http://localhost:3000`.

---

## Доступ к приложению

| Роль | URL |
|---|---|
| Пользователь | http://localhost:3000 |
| Администратор | http://localhost:3000/admin/dashboard |
| Модератор | http://localhost:3000/moderator/dashboard |

---

## Workflow

### Для пользователей

1. Регистрация через университетский email (`@phystech.edu`)
2. Создание объявлений о продаже б/у вещей
3. Поиск товаров с фильтрацией по категориям, цене, статусу
4. Бронирование товаров
5. Управление личным кабинетом: баланс (коины), история сделок, активные бронирования

### Для модераторов

1. Авторизация через панель модератора
2. Просмотр и модерация объявлений (одобрить / отклонить / удалить)
3. Управление пользователями при необходимости

### Для администраторов

1. Управление ролями пользователей
2. Применение санкций (заморозка / бан)
3. Управление балансами пользователей (коины)
4. Просмотр аудита действий

### Процесс продажи

```
Создание объявления → Модерация → Публикация → Бронирование покупателем → Подтверждение продавцом → Завершение сделки
```

---

## Email-уведомления

Система автоматически отправляет письма при следующих событиях:

| Событие | Получатель |
|---|---|
| Регистрация | Новый пользователь |
| Объявление одобрено / отклонено / удалено | Автор объявления |
| Бронирование создано | Покупатель и продавец |
| Продажа подтверждена | Покупатель |
| Бронирование отменено | Покупатель |
| Заморозка или бан аккаунта | Пользователь |
| Снятие ограничений | Пользователь |

---

## Особенности реализации

- Регистрация только по email МФТИ (`@phystech.edu`)
- Система внутренней валюты ("коины") для безопасных расчётов
- Автоматическая отмена бронирований старше 24 часов
- Модерация объявлений перед публикацией
- Полный аудит действий администраторов и модераторов

---


## Команда разработки

**Ментор:** Бобряков Д.С. ([@DmitryBobryakov](https://t.me/DmitryBobryakov))

**Разработчики:**
- Шабунина Анастасия ([@korsixe](https://t.me/korsixe))
- Орлова Елизавета ([@Liza30_06](https://t.me/Liza30_06))

---

## Планы по развитию
- бебебе
https://github.com/ArturOzolin/portal-frontend - Фронт
