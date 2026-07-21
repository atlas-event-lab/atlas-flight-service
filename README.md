# Atlas — Flight Service

> Owns the flight catalog and publishes catalog events.

Part of **[Atlas](https://github.com/atlas-event-lab)**.

## Responsibilities

- Manage the flight catalog (CRUD) and expose a service-readable price endpoint (ADR-0004).
- Publish catalog events so Inventory can seed capacity and Search can build its read model.

## Tech

Java 21 · Spring Boot · Spring Data JPA · PostgreSQL (`flight_db`) · Kafka · Keycloak JWT.

## API

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/flights` · `/api/v1/flights/{flightId}` | List / fetch flights |
| POST · PUT · DELETE | `/api/v1/flights` · `/api/v1/flights/{flightId}` | Manage catalog |
| GET | `/api/v1/flights/{flightId}/price` | Service-readable price (ADR-0004) |
| POST | `/api/v1/flights/reconciliation` | Catalog resync trigger (ADR-0025) |

## Events

**Produces:** `flight.created`, `flight.updated`, `flight.deleted`. **Consumes:** none
(catalog source of truth).

## Data

Owns `flight_db` (database-per-service).

## Patterns

Transactional outbox · catalog resync — republish current state from `flight_db` for
read-model rebuild beyond Kafka retention (ADR-0025).

## Run locally

```bash
docker compose up flight-service
```

Env: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `KAFKA_BOOTSTRAP_SERVERS`, `KEYCLOAK_ISSUER_URI`.

## License

Apache-2.0 — see [`LICENSE`](./LICENSE).
