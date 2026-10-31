# TicketHub Payment Service — V1

Payment-only microservice for TicketHub Phase 1 with Razorpay Test Mode.

## Source-of-truth scope

This implementation follows the TicketHub Product Requirements & Domain Blueprint v1.0 and the Student Development Backlog v1.0:

- Payment owns payment state and provider references.
- Payment amount comes from Booking Service; client amount is never trusted.
- V1 exposes payment initiation, payment status and provider webhook.
- Razorpay integration is behind a provider interface.
- Razorpay webhook signature is verified before processing.
- Repeated initiation is protected by `Idempotency-Key` and a unique `booking_id`.
- Duplicate/late webhook processing is state-safe.
- JWT authentication and ownership are enforced.
- Booking update is done through an API, never by touching Booking DB.
- Kafka publishes payment success/failure events. Transactional outbox is intentionally not included because it is a Phase 2 topic.
- Refund API is intentionally NOT part of this V1 implementation because the Blueprint/Backlog place refunds in Phase 2 (ST-72).

## Technology baseline

- Java 21
- Spring Boot 3.5.3
- Spring Data JPA
- MySQL 8.x
- Flyway
- Razorpay Java SDK 1.4.10
- Spring Security OAuth2 Resource Server / JWT
- Kafka
- Resilience4j
- Actuator + Micrometer
- OpenAPI / Swagger UI

Razorpay's current official Java SDK is 1.4.10 at the time this project was generated.

## APIs

### 1. POST /api/v1/payments

Headers:
- Authorization: Bearer <JWT>
- Idempotency-Key: unique client retry key
- Content-Type: application/json

Request:
```json
{
  "bookingId": "6f7b9f9e-4f4e-4f8c-8d34-4a3a8d9e0d11",
  "method": "UPI"
}
```

Important:
- No `amount` field is accepted.
- No `customerId` field is accepted.
- Booking Service is the source of truth for amount, currency, owner and PENDING_PAYMENT state.

Response:
```json
{
  "paymentId": "generated-uuid",
  "bookingId": "6f7b9f9e-4f4e-4f8c-8d34-4a3a8d9e0d11",
  "amount": 499.00,
  "currency": "INR",
  "status": "PENDING",
  "provider": "RAZORPAY",
  "method": "UPI",
  "providerReference": "order_xxx",
  "providerOrderId": "order_xxx",
  "providerPaymentId": null,
  "razorpayKeyId": "rzp_test_xxx",
  "createdAt": "...",
  "updatedAt": "..."
}
```

The client uses `razorpayKeyId`, `providerOrderId`, `amount` and `currency` to open Razorpay Checkout. The secret never leaves Payment Service.

### 2. GET /api/v1/payments/{paymentId}

The authenticated customer can only read their own payment.

### 3. POST /api/v1/payments/webhook

Headers:
- X-Razorpay-Signature: Razorpay HMAC signature

The raw request body is verified with the Razorpay webhook secret before JSON processing.

Handled events:
- `payment.captured` -> SUCCESS
- `order.paid` -> SUCCESS
- `payment.failed` -> FAILED

Unknown events are ignored safely.

## Booking Service contract required by Payment

Payment Service does not implement Booking Service. It requires these REST contracts:

### GET /api/v1/bookings/{bookingId}/payment-details

Expected response:
```json
{
  "bookingId": "...",
  "customerId": "authenticated-sub",
  "amount": 499.00,
  "currency": "INR",
  "status": "PENDING_PAYMENT",
  "expiryAt": "2026-10-04T16:30:00Z"
}
```

### POST /api/v1/bookings/{bookingId}/payment-result

Request:
```json
{
  "status": "SUCCESS",
  "providerReference": "pay_xxx",
  "providerPaymentId": "pay_xxx",
  "reason": null
}
```

Booking must treat this endpoint as idempotent:
- SUCCESS + SUCCESS => no-op
- FAILED + FAILED => no-op
- SUCCESS after FAILED => reject/ignore according to Booking state rules
- SUCCESS for EXPIRED => must not blindly confirm

## Razorpay setup

1. Create/use a Razorpay account in Test Mode.
2. Generate Test Mode Key ID and Key Secret.
3. Configure a webhook in Razorpay Dashboard pointing to:
   `https://<public-host>/api/v1/payments/webhook`
4. Use a separate webhook secret and set it as `RAZORPAY_WEBHOOK_SECRET`.
5. Subscribe to payment events required by the flow, especially captured and failed payment events.
6. Keep secret values only in environment variables / secret manager.

## Run locally

Prerequisites:
- MySQL 8.x running on localhost:3306
- Create database `tickethub_payment`
- Set `DB_USERNAME` and `DB_PASSWORD` in your environment or `.env`
- Docker Desktop for Kafka

Create the database:

```sql
CREATE DATABASE tickethub_payment CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Then run:

```bash
docker compose up -d
mvn clean test
mvn spring-boot:run
```

Application:
`http://localhost:8083`

Swagger:
`http://localhost:8083/swagger-ui.html`

Health:
`http://localhost:8083/actuator/health`

## Security

For normal customer APIs:
- JWT required.
- Customer identity comes from the authenticated principal.
- Client cannot submit `customerId`.
- GET payment uses `paymentId + customerId` ownership query.

Webhook:
- Authentication is not JWT-based because Razorpay calls it.
- It is protected by HMAC signature verification.

## Payment state

```text
INITIATED -> PENDING -> SUCCESS
                     \-> FAILED

CANCELLED / REFUNDED
    reserved for lifecycle extensions
```

A successful payment is not the final booking state. Payment calls Booking's payment-result contract, and Booking owns booking confirmation/seat booking.

## Razorpay amount handling

TicketHub stores amount as a decimal in the domain, e.g. `499.00`.
Razorpay Orders API expects the amount in currency subunits, so the service sends `49900` for INR 499.00.

Never accept a client-supplied final amount.

## Idempotency

Two protections are used:

1. `Idempotency-Key` unique database constraint.
2. One payment per `booking_id` unique database constraint.

This prevents client retries from creating multiple TicketHub payments.

Provider webhooks are also state-idempotent:
- repeated captured events do not create another payment;
- repeated failed events do not create another failure transition;
- a late failure cannot overwrite SUCCESS;
- a late success cannot overwrite FAILED.

## Phase 1 deliberate boundaries

Not included:
- refund API / automated refunds
- reconciliation jobs
- transactional outbox
- inbox/DLT/replay
- advanced Saga orchestration
- advanced late/out-of-order reconciliation
- custom payment processing
- direct access to Booking DB

Those belong to the later/advanced scope in the supplied documents.

## Package layout

```text
com.tickethub.payment
├── client
├── config
├── controller
├── dto
├── entity
├── event
├── exception
├── provider
├── repository
└── service
```


## MySQL schema notes

- MySQL is the Payment Service database for this version.
- Flyway owns schema creation/migration.
- Hibernate uses `ddl-auto: validate`; it does not modify the schema.
- Java UUID identifiers are stored as `CHAR(36)` for readability and easy cross-service debugging.
- PostgreSQL is not required by this project.
