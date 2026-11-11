# Academic Catalog API

Multi-tenant REST API to manage an academic catalog: **courses, subjects, teachers and teaching habilitations**. Built with Spring Boot 4, a contract-first OpenAPI design, cookie-based JWT authentication, role-based authorization and strict per-tenant data isolation on PostgreSQL.

![Java 21](https://img.shields.io/badge/Java-21-orange)
![Spring Boot 4.1](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Flyway-336791)

> **Status: work in progress.** The core domain is implemented and working. Automated tests and CI are still missing — see the [Roadmap](#roadmap--known-limitations).

## Features

- **Contract-first API.** The OpenAPI spec (`src/main/resources/openapi/academiccatalog-openapi.yaml`) is the source of truth. `openapi-generator` produces the controller interfaces and DTOs at build time; controllers just implement them.
- **Multi-tenancy.** Every row carries a `tenant_id`. The tenant is resolved server-side from the authenticated user, never from the request, and composite foreign keys `(tenant_id, id)` enforce isolation in the database as well.
- **Authentication & authorization.** Login issues a short-lived (15 min) HS256 JWT stored in an `httpOnly`, `SameSite=Lax` cookie. Method-level security (`@PreAuthorize`) with four roles: `CATALOG_ADMIN`, `CATALOG_EDITOR`, `CATALOG_READER`, `TEACHER`.
- **Business rules in the service layer.** Creating habilitations is transactional and validates that the teacher, course and subject are active, that the subject belongs to the course, and that no duplicate active habilitation exists (also inside a single request).
- **Database integrity.** Flyway migrations, unique indexes per tenant (course code, subject code per course, case-insensitive teacher email) and a partial unique index guaranteeing one *active* habilitation per teacher/course/subject.
- **Consistent error model.** `404` not found, `409` conflict, `400` validation, `403` forbidden, and a generic `500` that never leaks internal messages.
- **Soft deletes.** Courses and subjects are deactivated, not removed, to preserve history.

## Tech stack

| Area | Technology |
|---|---|
| Language / runtime | Java 21 |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, Validation, Actuator) |
| Security | Spring Security, OAuth2 Resource Server (JWT), BCrypt |
| Database | PostgreSQL, Flyway migrations |
| API contract | OpenAPI 3.1, openapi-generator (`spring`, `interfaceOnly`) |
| Other | Lombok, Maven |

## Architecture

```
 Request (cookie: access_token)
        │
        ▼
 BearerTokenAuthenticationFilter   ← validates the JWT from the cookie
        │
        ▼
 TenantFilter                      ← loads the user, sets TenantContext + RoleContext
        │
        ▼
 Controller  (@PreAuthorize roles) ← implements the generated *Api interface
        │
        ▼
 Service     (business rules, @Transactional)  ← always scoped by tenantId
        │
        ▼
 Repository (Spring Data JPA)  →  PostgreSQL (composite FKs, unique indexes)
```

Key decisions:

- **Tenant per column, not per schema.** Simple to operate; isolation is enforced both in queries (`tenantId` in every specification/finder) and in the schema (composite foreign keys).
- **Tenant context via a servlet filter.** `TenantFilter` stores the tenant and roles in thread-locals and always clears them in a `finally` block.
- **Constraints in the database, not only in code.** Service-level checks give friendly `409` messages; unique indexes close the race condition between concurrent requests.

## Getting started

### Prerequisites

- JDK 21
- PostgreSQL (any recent version) with an empty database
- Maven (or use the included `./mvnw`)

### Configuration

Copy `.env.example` and set the values as environment variables (or in your IDE run configuration):

| Variable | Description |
|---|---|
| `DATASOURCE_URL` | e.g. `jdbc:postgresql://localhost:5432/postgres` |
| `DATASOURCE_USERNAME` / `DATASOURCE_PASSWORD` | Database credentials |
| `JWT_SECRET` | Base64 HMAC key, at least 32 bytes. Generate one with `openssl rand -base64 32` |
| `SPRING_PROFILES_ACTIVE` | Set to `dev` to seed sample data (optional) |
| `SEED_PASSWORD` | Password for the sample users (required when seeding) |
| `SECURITY_COOKIE_SECURE` | Defaults to `true`. Set to `false` to use the cookie over plain `http://localhost` |

### Run

```bash
./mvnw spring-boot:run
```

On startup Flyway applies the migrations (`V1`–`V4`) and the OpenAPI interfaces are generated during the build.

With the `dev` profile and `SEED_PASSWORD` set, these users are created (all with that password):

| User | Tenant | Role |
|---|---|---|
| `admin@tenant-a.example` | A | `CATALOG_ADMIN` |
| `editor@tenant-a.example` | A | `CATALOG_EDITOR` |
| `admin@tenant-b.example` | B | `CATALOG_ADMIN` |

Tenant B exists so you can verify that its users cannot see tenant A's data.

## Usage example

```bash
# 1. Log in (the JWT is stored in the cookie jar)
curl -c cookies.txt -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@tenant-a.example","password":"<SEED_PASSWORD>"}'

# 2. Who am I?
curl -b cookies.txt http://localhost:8080/api/auth/whoami

# 3. Create a course, a subject and a teacher
curl -b cookies.txt -X POST http://localhost:8080/courses \
  -H "Content-Type: application/json" \
  -d '{"code":"MATH-1","name":"Mathematics I"}'

curl -b cookies.txt -X POST http://localhost:8080/courses/<courseId>/subjects \
  -H "Content-Type: application/json" \
  -d '[{"code":"ALG","name":"Algebra","hoursPerWeek":4,"maxHoursPerDay":2}]'

curl -b cookies.txt -X POST http://localhost:8080/teachers \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Ada","lastName":"Lovelace","email":"ada@example.com"}'

# 4. Habilitate the teacher for that subject (repeat it to get a 409)
curl -b cookies.txt -X POST http://localhost:8080/teachers/<teacherId>/habilitations \
  -H "Content-Type: application/json" \
  -d '[{"courseId":"<courseId>","subjectId":"<subjectId>"}]'
```

## Endpoints

| Method | Path | Roles |
|---|---|---|
| POST | `/api/auth/login` | public |
| POST | `/api/auth/logout` | public |
| GET | `/api/auth/whoami` | authenticated |
| GET | `/courses`, `/courses/{id}` | admin, editor, reader, teacher |
| POST | `/courses` | admin, editor |
| PATCH | `/courses/{id}` | admin, editor |
| DELETE | `/courses/{id}` (deactivate) | admin |
| POST | `/courses/{id}/activation` (reactivate) | admin |
| GET | `/courses/{id}/subjects` | admin, editor, reader, teacher |
| POST, PATCH | `/courses/{id}/subjects` (create / bulk update) | admin, editor |
| GET, POST | `/teachers` | admin, editor |
| PATCH | `/teachers/{id}` | admin, editor |
| GET, POST | `/teachers/{id}/habilitations` | admin, editor |

List endpoints are paginated (`page`, `size`) and the full request/response schemas live in the OpenAPI spec.

## Project structure

```
src/main/java/com/example/api
├── auth/      security config, JWT login, TenantFilter, tenant/role context
└── catalog/   controller · service · repository · entity · mapper · exception
src/main/resources
├── openapi/   academiccatalog-openapi.yaml (API contract)
└── db/migration/   Flyway migrations
```

## Roadmap / known limitations

This project is under active development. Known gaps:

- **No automated tests yet** beyond a context-load smoke test. Planned: integration tests with Testcontainers (tenant isolation, roles, habilitation rules).
- **No CI pipeline** yet (GitHub Actions planned).
- `create*` endpoints return `200`; they should return `201` with a `Location` header.
- `sort` / `direction` query parameters are not whitelisted yet.
- `status` / `effective` filters on teacher and habilitation listings are accepted but not applied yet.
- Several operations in the spec are still commented out (get/deactivate teacher, revoke habilitation, get subject, …).
- No refresh tokens or server-side token revocation (logout only clears the cookie).
- No Docker setup yet.

## License

Not specified yet.
