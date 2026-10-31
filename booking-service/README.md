# TicketHub – Booking Service

Owns **bookings and seat-reservation state** (Product Blueprint §4). Port `8084`, database `tp_snapseat_booking` (MySQL).

Flow: `Reserve seats → PENDING_PAYMENT (expiry) → Payment result → CONFIRMED / PAYMENT_FAILED / EXPIRED / CANCELLED → BookingConfirmed event`

## Status models
| Object | States |
|---|---|
| Booking | `PENDING_PAYMENT → CONFIRMED / PAYMENT_FAILED / CANCELLED / EXPIRED` (only PENDING_PAYMENT can move; enforced in `Booking.changeStatus`, every change audited in `booking_status_history`) |
| Seat reservation (`show_seats`) | `AVAILABLE → RESERVED → BOOKED` (RESERVED → AVAILABLE on cancel / fail / expiry) |

## API
Customer API (JWT, role `CUSTOMER` or `ADMIN`) – identity is the token `sub`; there is **no** customerId/amount in any request.

| Method | Path | Notes |
|---|---|---|
| POST | `/api/bookings` | Header `Idempotency-Key` **required**. Body `{ "showId": 1, "seatIds": [1,2] }`. 201 + `PENDING_PAYMENT` booking. Retry with same key returns the original booking (SC-13). |
| GET | `/api/bookings/{id}` | Own booking only (others → 404). ADMIN can view any. Returns status, seats, amount, currency, expiresAt. |
| GET | `/api/bookings?page=0&size=20` | My bookings, newest first. |
| POST | `/api/bookings/{id}/cancel` | Cancels a `PENDING_PAYMENT` booking and releases seats. `CONFIRMED` → 409 (refunds are Phase 2). |

Service-to-service API (not routed via gateway):

| Method | Path | Caller | Notes |
|---|---|---|---|
| POST | `/internal/bookings/{id}/payment-result` | Payment Service, role `SERVICE` | Body `{ "paymentId": "...", "status": "SUCCESS"\|"FAILED", "amount": 500.00 (optional check) }`. Idempotent. Response `{ bookingId, bookingStatus, outcome }` where outcome ∈ `CONFIRMED, PAYMENT_FAILED_RECORDED, ALREADY_PROCESSED, REJECTED_EXPIRED, REJECTED_NOT_PENDING, DUPLICATE_PAYMENT`. |
| GET | `/internal/shows/{showId}/seat-states` | Event/Show Service | Lists `RESERVED` / `BOOKED` seats; every other seat is `AVAILABLE`. Expired reservations are not reported. |

Errors always use one format: `{ timestamp, status, error, code, message, path, correlationId, details[] }`.
Codes: `VALIDATION_FAILED, INVALID_REQUEST, BOOKING_NOT_FOUND, SHOW_NOT_FOUND, SHOW_NOT_BOOKABLE, SEAT_NOT_AVAILABLE (409), INVALID_SEAT_SELECTION, INVALID_BOOKING_STATE (409), IDEMPOTENCY_KEY_REUSED (409), AMOUNT_MISMATCH (409), DEPENDENCY_UNAVAILABLE (503), UNAUTHENTICATED (401), ACCESS_DENIED (403)`.

Swagger UI: `/swagger-ui.html` · OpenAPI JSON: `/v3/api-docs`.

## How the other services plug in
**Payment Service** (`POST /payments`): call `GET /api/bookings/{id}` forwarding the customer's token (ownership enforced), take `amount`/`currency`/`status`/`expiresAt` from it – only start payment for `PENDING_PAYMENT`. After the verified provider result (or webhook), call `POST /internal/bookings/{id}/payment-result` with a client-credentials token that has the `SERVICE` role. Booking never calls Payment.

**Event/Show Service** must provide (Booking's own DTOs: `client/ShowResponse`, `client/SeatLayoutResponse`):
- `GET /api/shows/{showId}` → `{ showId, eventId, venueId, status, eventStatus, ticketPrice, currency, startTime, endTime }` (404 if unknown)
- `GET /api/shows/{showId}/seat-layout` → `[ { seatId, row, number, category } ]` – layout only, no reservation state (so Event ↔ Booking never call each other in a loop)
- For `GET /shows/{id}/seats` it calls `GET /internal/shows/{showId}/seat-states` here and merges with the layout.

**Notification Service** consumes topic `booking.confirmed` (key = bookingId) in its own consumer group. Event contract (v1):
```json
{ "eventId": "uuid (deterministic per booking)", "eventType": "BookingConfirmed", "eventVersion": 1,
  "bookingId": 12, "customerId": "<jwt sub>", "customerEmail": "<jwt email claim or null>",
  "showId": 77, "seatIds": [1,2], "amount": 500.00, "currency": "INR", "occurredAt": "2026-10-05T10:05:00Z" }
```
`eventId` is stable per booking, so a re-published event can be de-duplicated (SC-08). The event is sent **after** the DB commit; a Kafka failure is logged (`BookingConfirmed publish FAILED`) and never changes the booking (SC-07). Guaranteed publishing = outbox, Phase 2.

## Rules implemented (Blueprint → code)
| Rule | Where |
|---|---|
| Valid, bookable show (OPEN show of PUBLISHED event) | `BookingServiceImpl.validateShow` |
| Seats must belong to the show layout | `validateSeatsBelongToShow` |
| BOOKED / actively RESERVED seat can't be reserved (SC-02) | `ShowSeat.isAvailableAt` |
| Simultaneous requests: exactly one wins (SC-03, ST-08) | unique `(show_id, seat_id)` + `PESSIMISTIC_WRITE` locks in ascending seat order + `SeatInitializer` (race-safe row creation). Proven by `SeatConcurrencyIT` |
| Amount derived from show price × seats, stored as snapshot (ST-46) | `reserve()`, `bookings.amount`, `booking_seats.price` |
| Reservation expiry, configurable (ST-09) | `booking.reservation.expiry`, `ReservationExpiryJob` |
| Expired booking is never confirmed blindly (SC-12) | `applySuccess` → `REJECTED_EXPIRED` |
| Payment failure frees seats (SC-04) | `applyFailure` |
| Duplicate / late payment callbacks are harmless (SC-10) | `ALREADY_PROCESSED`, late FAILED never undoes CONFIRMED |
| Client retry doesn't duplicate (SC-13) | `Idempotency-Key` + unique `(customer_id, idempotency_key)` |
| Identity from token, ownership in the query (ST-14/59) | `CurrentUser`, `findByBookingIdAndCustomerId` |
| Timeout / bounded retry / circuit breaker on Event calls (SC-05/16) | `application.yml`, `EventClientConfig`, `EventServiceFallbackFactory` |
| Notification never blocks or rolls back booking (SC-07) | Kafka after commit, no Notification client |
| Logs: service name, correlationId, bookingId, paymentId; metrics for Prometheus | ECS JSON logging, `CorrelationIdFilter`, Actuator `/actuator/prometheus` |

## Configuration (env vars, defaults in `application.yml`)
`DB_URL, DB_USERNAME, DB_PASSWORD, EVENT_SERVICE_URL, JWK_SET_URI, ISSUER_URI, KAFKA_BOOTSTRAP_SERVERS, BOOKING_EXPIRY (e.g. 10m)`.
Keycloak realm `tickethub` needs realm roles `CUSTOMER`, `ADMIN`, `SERVICE` (the last for Payment's service account). Roles are read from `realm_access.roles`.

## Run / test
```
./mvnw spring-boot:run          # needs MySQL, (Keycloak for tokens), Kafka for publishing
./mvnw test                     # unit tests + H2 concurrency test, no external services
```
Set `booking.events.enabled=false` to run without Kafka.

## Deliberately out of scope (Phase 2 / other services)
Refund of a confirmed booking, saga/reconciliation of "paid but not confirmed", transactional outbox, DLT/retry topics, service discovery (Eureka lines are commented in `pom.xml`/config until S6), Flyway migrations (`ddl-auto: update` is dev-only).
