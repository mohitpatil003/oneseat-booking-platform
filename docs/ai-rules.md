# AI rules for OneSeat

Stack: Java 21, Spring Boot 3, Maven, PostgreSQL, Redis, Kafka, Flyway.

- Constructor injection only; DTOs at API boundaries; no business logic in controllers.
- Every Kafka consumer is idempotent.
- Every new class gets JUnit 5 tests; use Testcontainers for integration tests.
- Never hardcode secrets; read them from environment variables.
- Write the failing test first, then the minimal code. Do not touch unrelated files.
- Read docs/requirements.md before implementing a story. One change per user story or task.
- booking_db (Postgres) is the source of truth for sold seats. Redis holds only temporary seat holds.
- Confirm and payment are idempotent through the Idempotency-Key header.
- Package roots: com.oneseat.booking, com.oneseat.payment, com.oneseat.notification.
- Each service is a standalone Maven project with its own pom.xml and mvnw; no parent pom, no shared library.
- Each service owns its database; never read tables of another service or import its classes.
- Services talk only through REST and Kafka; pass a correlation id header on every call.
- Layers per service: controller, service, dto, entity, repository, mapper, exception, config, common (plus redis, client, messaging, provider, mail where needed).
- Controllers handle HTTP only. Services hold business logic and transactions and return DTOs, never entities. Repositories contain queries only.
- DTOs are Java records with validation annotations. Entities never appear in API responses.
- Dependencies point one way: controller to service to repository, client, redis or messaging. Entities import no other layer.
- Naming: XxxController, XxxService, XxxRepository, XxxRequest, XxxResponse, XxxMapper, XxxException.
- Test layout mirrors main packages: WebMvcTest for controllers, Mockito unit tests for services, Testcontainers for repositories and integration tests.