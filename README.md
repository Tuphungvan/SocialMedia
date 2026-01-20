# Social Media Backend

## Chạy dự án

### 1. Start services
```bash
docker-compose up -d
```

### 2. Chạy app
```bash
mvn spring-boot:run
```

## Services

- **API**: http://localhost:8080
- **PostgreSQL**: localhost:5432 (user: admin, pass: admin123)
- **Redis**: localhost:6379
- **Kafka**: localhost:9092
- **MailHog**: http://localhost:8025

## Dừng services
```bash
docker-compose down
```
