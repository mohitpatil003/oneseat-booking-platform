# AI rules for OneSeat
Stack: Java 21, Spring Boot 3, Maven, PostgreSQL, Redis, Kafka, Flyway.
- Constructor injection only; DTOs at API boundaries; no business logic in controllers.
- Every Kafka consumer is idempotent.
- Every new class gets JUnit 5 tests; use Testcontainers for integration tests.
- Never hardcode secrets; read them from environment variables.
- Write the failing test first, then the minimal code. Do not touch unrelated files.
