# AI rules for OneSeat
Stack: Java 21, Spring Boot 3, Maven, PostgreSQL, Redis, Kafka, Flyway.
- Constructor injection only; DTOs at API boundaries; no business logic in controllers.
- Every Kafka consumer is idempotent.
- Every new class gets JUnit 5 tests; use Testcontainers for integration tests.
- Never hardcode secrets; read them from environment variables.
- Write the failing test first, then the minimal code. Do not touch unrelated files.
- Read docs/requirements.md before implementing a story. One change per user story or task.
- Postgres is the source of truth for sold seats. Redis holds only temporary seat holds.
- Hold expiry uses Redis TTL. All times are UTC.
- Confirm and payment must be idempotent through the Idempotency-Key header.
- Packages: com.oneseat.<module>. Flyway files: V<number>__description.sql. Kafka topics: lowercase, dot-separated (booking.confirmed).
- Commit messages: type: short description (feat, fix, docs, test, chore, refactor).
- Each service owns its database; never read another service's tables. Services talk only through REST and Kafka.
- Pass a correlation id header on every service call.
