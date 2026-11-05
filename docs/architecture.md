# OneSeat - Architecture

## Repository structure

One Git repository containing three independent Maven projects. There is no root pom.xml,
no parent pom and no shared library.

    oneseat-booking-platform/
    |-- README.md
    |-- docker-compose.yml         shared infrastructure only (Postgres, Redis, Kafka)
    |-- infra/postgres/            init-databases.sql (booking_db, payment_db, notification_db)
    |-- docs/                      requirements, architecture, adr/, api/, diagrams/
    |-- k8s/                       infra/, booking-service/, payment-service/, notification-service/
    |-- load-tests/                k6 scripts and results/
    |-- .github/workflows/         one workflow per service
    |-- booking-service/           standalone project, port 8081, com.oneseat.booking
    |-- payment-service/           standalone project, port 8082, com.oneseat.payment
    |-- notification-service/      standalone project, port 8083, com.oneseat.notification

## Independence rules
- Each service has its own pom.xml (Spring Boot starter parent), Maven wrapper, Dockerfile, CI workflow and README.
- Each service owns one database and its own Flyway migrations.
- Services never import each other's classes. They meet only through REST and Kafka.
- Configuration comes from environment variables, with defaults in application.yml.
- Every service call carries an X-Correlation-Id header.

Note: the service folders and docker-compose.yml are created from Day 8 onwards (booking-service Day 8, payment-service Day 16, notification-service Day 20).