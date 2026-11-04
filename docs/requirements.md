# OneSeat - Requirements

## 1. Problem statement
Popular events sell out in seconds. Naive booking systems oversell seats, double-charge
customers when payments time out, and fail under concurrent load. OneSeat guarantees
that a seat is sold exactly once, even with thousands of users competing for it.

## 2. Personas
- **Attendee:** wants to hold seats quickly and pay without being charged twice.
  Pain: crashes at checkout, lost holds, duplicate charges on retries.
- **Organizer:** wants to create events and see accurate seat inventory.
  Pain: oversold seats and wrong inventory during traffic bursts.
- **Admin:** wants to see system health, errors and throughput.
  Pain: unclear system state during failures.

## 3. User stories and acceptance criteria

### US-1 Browse seat availability (Attendee)
As an attendee, I want to see which seats are available so that I can pick seats.
- [ ] Given an event id, the API returns each seat as AVAILABLE, HELD or SOLD.
- [ ] Held seats whose hold has expired show as AVAILABLE.

### US-2 Hold seats (Attendee)
As an attendee, I want to hold up to 4 seats for 5 minutes so that nobody else can take them.
- [ ] A request for more than 4 seats is rejected.
- [ ] If any requested seat is unavailable, no seat is held (all or nothing).
- [ ] A successful hold returns a hold token and expires after 5 minutes.
- [ ] Under concurrent requests, two users never hold the same seat.

### US-3 Confirm a booking safely (Attendee)
As an attendee, I want to confirm my held seats with an Idempotency-Key so that retries never charge me twice.
- [ ] Confirm requires a valid hold token owned by the caller.
- [ ] A repeated request with the same Idempotency-Key returns the original result and does not charge again.
- [ ] The same key with a different request body is rejected.
- [ ] Confirming after the hold has expired is rejected.

### US-4 Handle payment failure (Attendee)
As an attendee, I want failed payments to free my seats without charging me so that I can try again.
- [ ] If payment fails or times out, the booking is marked FAILED and the hold is released.
- [ ] A payment is never charged twice for the same booking.

### US-5 Release seats (Attendee, System)
As the system, I want expired and cancelled holds to return seats to the pool so that other buyers can book them.
- [ ] An expired hold makes the seats AVAILABLE again.
- [ ] Only the hold owner can release a hold manually, and release is immediate.

### US-6 Create an event (Organizer)
As an organizer, I want to create an event with seats so that attendees can book them.
- [ ] Creating an event with a seat count generates that many seats.
- [ ] Invalid input returns a clear error.

### US-7 View system metrics (Admin)
As an admin, I want to see booking, conflict and error counts so that I can judge system health.
- [ ] Metrics for holds, conflicts, confirmed bookings and payment failures are exposed.

## 4. Scope (MoSCoW)

**Must**
- US-1 to US-6
- No oversold seats, enforced by a Redis hold plus a database unique constraint
- Idempotent confirm and payment
- Kafka event on booking confirmation, published through a transactional outbox

**Should**
- Notification consumer with retries and a dead-letter topic
- Rate limiting on the hold endpoint
- Metrics and dashboards (US-7)
- Job that expires stale pending bookings

**Could**
- Virtual waiting room
- JWT authentication
- Cancellation and refunds after confirmation
- Seat map UI and multi-tier pricing

**Won't (this version)**
- Single sign-on, barcode tickets, surge pricing
- A real payment gateway (a simulated provider is used)
- A frontend (Swagger UI is the interface)

## 5. Non-functional requirements

Targets are provisional. They are re-checked against real measurements on Day 25.
Always record the hardware beside every result.

| ID | Area | Target | How it is verified |
|---|---|---|---|
| NFR-1 | Correctness | 0 oversold seats and 0 double charges in every load and chaos run | 200-thread concurrency test; k6 flash-sale run; SQL check that no seat has more than one booking |
| NFR-2 | Hold latency | p95 under 300 ms at target load | k6 results |
| NFR-3 | Confirm latency | p95 under 500 ms, including a simulated payment of 100 ms or less | k6 results |
| NFR-4 | Availability query latency | p95 under 150 ms | k6 results |
| NFR-5 | Throughput | 1,000 or more requests per second sustained for 60 seconds | k6 results |
| NFR-6 | Idempotency | 100% of retries with the same key return the original result; 0 duplicate charges | Integration tests with duplicate and concurrent retries |
| NFR-7 | Resilience | No lost, duplicated or stuck bookings when the app, Redis, Kafka or Postgres restarts mid-flow | Chaos tests (Days 27 and 28) |
| NFR-8 | Recovery | Stalled work resumes within 30 seconds of the dependency coming back | Chaos tests with timestamps |
| NFR-9 | Hold expiry | Expired holds make seats available again within 1 second; stale pending bookings are expired within 60 seconds | Integration tests with short TTLs |
| NFR-10 | Security | No secrets in the repo; hold endpoint rate-limited (HTTP 429 above the limit); all input validated | Gitleaks scan; rate-limit tests |
| NFR-11 | Observability | Metrics for holds, conflicts, bookings, payment failures and Kafka consumer lag | Prometheus endpoint check |
| NFR-12 | Operability | The whole system starts with one command and deploys to Kubernetes from manifests | README walkthrough on a clean checkout |

## 6. Assumptions, constraints and out of scope

### Assumptions
- One region and one primary Postgres database; no multi-region.
- Postgres is the source of truth for sold seats. Redis only holds temporary seat holds.
- Payment is a simulated provider with configurable latency, failures and timeouts. No real money moves.
- An event has up to 10,000 seats. A hold covers up to 4 seats and lasts 5 minutes.
- All servers use UTC. Hold expiry uses Redis TTL, not clocks compared in application code.
- Users are identified by a user id in the request. JWT authentication is a Could item.
- Load test numbers come from a single 8 GB laptop acting as both client and server, so they are indicative, not production numbers.

### Constraints
- Stack: Java 21, Spring Boot 3, PostgreSQL, Redis, Kafka, Docker, Kubernetes.
- Free and open-source tools only; AI code generation through cloud APIs.
- Time budget: 12 hours a week for 5 weeks (60 hours).
- Memory: the laptop runs one of three modes at a time (daily development, Kubernetes, pipeline and monitoring).

### Out of scope
- Single sign-on, barcode tickets, surge pricing and a seat-map UI.
- A real payment gateway and real email or SMS delivery (both are mocked).
- Multi-region deployment and database sharding (discussed in the README as future work only).

## 7. Top 5 risks and mitigations

| # | Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|---|
| R1 | Concurrency bugs in hold and confirm logic take longer than planned | High | High | Write tests first; run a small concurrent test from Day 13, not only Day 22; keep the database unique constraint as the final safety net; review all locking code by hand |
| R2 | 8 GB laptop memory limits load tests and Kubernetes | High | Medium | Use the memory modes; cap Kafka and JVM heaps; lower virtual users; record hardware; use the cut list |
| R3 | AI-generated code hides bugs or invents APIs | High | High | Commit before each agent run; require tests; check library names against official docs; review payment, locking and SQL code line by line |
| R4 | Scope and schedule slip (12 h a week alongside a job and interview prep) | Medium | High | Weekly retro; cut from the bottom of the cut list; never cut concurrency tests, load numbers or the README |
| R5 | AI API limits, outages or an empty balance block coding days | Medium | Medium | Keep a second provider key; write prompts small; do documentation and tests on days the API is down |

## 8. Test and measurement plan

### Test types
| Type | Tools | Covers |
|---|---|---|
| Unit | JUnit 5, Mockito | Business rules, state transitions |
| Integration | Testcontainers (Postgres, Redis, Kafka) | Hold, confirm, idempotency, outbox, consumers |
| Concurrency | ExecutorService with CountDownLatch (200 threads) | NFR-1: exactly one winner per seat |
| Load | k6 | NFR-2 to NFR-5 |
| Chaos | docker stop, process kill, under load | NFR-7 and NFR-8 |
| Security and quality | Gitleaks, Trivy, JaCoCo | NFR-10, image and secret scanning, coverage |

### How measurements are taken
- Record hardware, git commit, k6 script name and date next to every result.
- Warm up for 30 seconds, then measure for 60 seconds. Run each scenario 3 times and report the median p95.
- Compare the naive version (check, then insert, no hold, no constraint) with the fixed version at 100, 500 and 1,000 virtual users.
- Save results as load-tests/results/YYYY-MM-DD-label.md. Never quote a number that is not saved there.

### Failure scenarios
| ID | Scenario | Expected behavior |
|---|---|---|
| FS-1 | Redis restarts while holds exist | Holds are lost; seats return to AVAILABLE; confirm rejects a missing hold; nothing is double-sold |
| FS-2 | Client retries confirm while the first request is still running | Only one request is processed; the other waits or returns the same result |
| FS-3 | Payment succeeds but the response times out | The retry returns the same result; the customer is charged once |
| FS-4 | App crashes after the booking commit but before the Kafka publish | The outbox publisher sends the event after restart; no event is lost |
| FS-5 | Kafka is down for 60 seconds | Outbox rows accumulate, then drain after recovery; no bookings are affected |
| FS-6 | Consumer receives the same message twice | The processed-events table makes it a no-op |
| FS-7 | Poison message fails every retry | It moves to the dead-letter topic after the maximum attempts |
| FS-8 | Postgres restarts during confirm | The transaction rolls back; the hold stays until TTL; a retry succeeds |
| FS-9 | Hold expires while payment is in flight | Confirm only starts payment if enough hold time remains; otherwise it is rejected |
| FS-10 | 1,000 users request the same seat at once | Exactly one succeeds; the rest get 409 quickly |
| FS-11 | Expiry sweeper runs on two instances at once | Each stale booking is expired exactly once |
| FS-12 | Same idempotency key sent with a different request body | Rejected with a clear error; nothing is charged |
