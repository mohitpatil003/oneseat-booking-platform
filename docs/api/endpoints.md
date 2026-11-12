# OneSeat - API Endpoint List

## Scope

This is the Day 5 endpoint inventory. It records the planned REST endpoints and their purpose. Detailed OpenAPI definitions, request/response schemas, validation rules and error examples are part of Day 6.

## Common headers

- `X-Correlation-Id`: carried on every request and every inter-service call.
- `Idempotency-Key`: required on booking confirmation. Payment-service uses the booking ID as the charge idempotency key.
- `userId`: supplied with booking operations that need to identify the caller and verify hold ownership. JWT authentication is out of scope for this version.

## booking-service - Port 8081

| Method   | Endpoint                  | Purpose                                                                                            |
|----------|---------------------------|----------------------------------------------------------------------------------------------------|
| `POST`   | `/events`                 | Create an event and generate its seats.                                                            |
| `GET`    | `/events/{eventId}/seats` | Return seat availability as `AVAILABLE`, `HELD` or `SOLD`.                                         |
| `POST`   | `/events/{eventId}/holds` | Hold one to four seats for five minutes; the operation is all-or-nothing and returns a hold token. |
| `DELETE` | `/holds/{holdToken}`      | Release a hold immediately after verifying its owner.                                              |
| `POST`   | `/bookings/confirm`       | Confirm a valid hold, create a booking and initiate payment. Requires `Idempotency-Key`.           |
| `GET`    | `/bookings/{bookingId}`   | Retrieve the booking status.                                                                       |

## payment-service - Port 8082

| Method | Endpoint               | Purpose                                                                         |
|--------|------------------------|---------------------------------------------------------------------------------|
| `POST` | `/charges`             | Create or return the stored charge result. `Idempotency-Key` is the booking ID. |
| `GET`  | `/charges/{bookingId}` | Return the stored payment result for booking reconciliation.                    |

## notification-service - Port 8083

| Method | Endpoint           | Purpose                                                                                                      |
|--------|--------------------|--------------------------------------------------------------------------------------------------------------|
| `GET`  | `/actuator/health` | Report service health. Notification delivery is performed by the Kafka consumer, not a public REST endpoint. |

## Notes

- Booking-service is the public booking API. Its call to payment-service is an internal REST call.
- Notification-service consumes `booking.confirmed` from Kafka; it does not expose a notification-creation endpoint.
- Request/response fields, HTTP error codes and OpenAPI files are defined on Day 6.
