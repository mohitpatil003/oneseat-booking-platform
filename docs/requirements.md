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
