# 🎮 Up Leveling

![CI](https://github.com/RodrigodSantos/up-leveling/actions/workflows/ci.yml/badge.svg)

Hábitos gamificados: cumpra seus hábitos, ganhe XP e suba de nível.

> 🚧 **Em reconstrução.** O backend está sendo refeito do zero (Java 21 + Spring Boot 4) e depois ganha um frontend completo.

## 🛠️ Stack
- **Java 21** + **Spring Boot 4.1**
- **Spring Data JPA** + **PostgreSQL 17**
- **Flyway** para versionar o schema
- **springdoc-openapi** (Swagger UI)
- **JUnit** + **Testcontainers** (testes contra um Postgres real)
- **Docker Compose** e **GitHub Actions** + **JaCoCo**

## 🚀 Como rodar
Pré-requisitos: Java 21 e Docker.

```bash
# 1. Sobe o Postgres (porta 5434)
docker compose up -d

# 2. Sobe a API
./mvnw spring-boot:run
```

- Swagger: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

### Testes
```bash
./mvnw verify
```
O Docker precisa estar rodando: o Testcontainers sobe um Postgres temporário para os testes.

## 🗺️ Roadmap
- [x] Fundação: Java 21, Spring Boot 4, Flyway, Swagger, erros no padrão RFC 9457, Testcontainers, CI
- [x] Usuários e autenticação (JWT): cadastro, login, `/api/me` e troca de senha
- [x] Hábitos com agenda semanal: CRUD, pausar/reativar e exclusão lógica
- [ ] Gamificação: check-in, XP e níveis
- [ ] Endpoints de progresso para o frontend
- [ ] Frontend
- [ ] Deploy
