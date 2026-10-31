# Event Service — PRD Changes Applied

The Event Service has been updated to align with the TicketHub PRD/domain blueprint.

## Applied changes

- Replaced event `active` flag with `EventStatus`:
  - `DRAFT`
  - `PUBLISHED`
  - `CANCELLED`
- New events are created as `DRAFT`.
- Added event publish/cancel endpoints.
- Public event listing returns only `PUBLISHED` events.
- Cancelled events cannot be updated or published.
- Replaced show statuses with:
  - `SCHEDULED`
  - `OPEN`
  - `SOLD_OUT`
  - `CANCELLED`
  - `COMPLETED`
- Replaced seat `HELD` with `RESERVED`.
- Added paginated `GET /api/shows`.
- Show creation/update now requires a `PUBLISHED` event and an active screen.
- Added self-excluding show overlap validation for updates.
- Cancelled shows are ignored for overlap checks.
- Show creation now initializes `createdAt`.
- Added `GET /api/shows/{showId}/seats`.
- Existing `ShowSeat` optimistic-locking (`@Version`) and unique show/seat constraint are preserved.
- Booking/reservation workflow remains outside Event Service, as required by the PRD.

## Database note

The project currently uses `spring.jpa.hibernate.ddl-auto: update` and has no existing Flyway migration scripts in the uploaded project. Because `events.active` is being replaced by the non-null `events.status` column, an existing database with event rows needs a data migration before deploying this version.

For a fresh database, Hibernate can create/update the new column. For an existing database, migrate existing rows to an appropriate status (normally `PUBLISHED` for previously active public events and `CANCELLED` for previously inactive ones), then remove the old `active` column according to your deployment migration process.

## Build verification

A full Maven build could not be run in this environment because the uploaded Maven wrapper is incomplete (the `.mvn/wrapper/maven-wrapper.properties` file is missing) and no system `mvn` executable is installed. Source-level checks were performed and no stale Event/Show/Seat enum references were found in the Java source.
