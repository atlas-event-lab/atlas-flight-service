-- Flight catalog: Flight aggregate root + FlightSegment legs.
-- airline_id / *_airport_id are LOCAL lookup references (ARCH-004), never cross-service FKs.

CREATE TABLE flights
(
    id                     UUID                     NOT NULL,
    flight_number          VARCHAR(20)              NOT NULL,
    airline_id             UUID                     NOT NULL,
    origin_airport_id      UUID                     NOT NULL,
    destination_airport_id UUID                     NOT NULL,
    departure_time         TIMESTAMP WITH TIME ZONE NOT NULL,
    arrival_time           TIMESTAMP WITH TIME ZONE NOT NULL,
    total_seats            INTEGER                  NOT NULL,
    base_price_amount      NUMERIC(19, 2)           NOT NULL,
    currency               VARCHAR(3)               NOT NULL,
    status                 VARCHAR(20)              NOT NULL,
    created_at             TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at             TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_flights PRIMARY KEY (id),
    -- Business uniqueness key (services/flight/service.md): a duplicate create returns 409.
    CONSTRAINT uq_flights_number_departure UNIQUE (flight_number, departure_time)
);

CREATE INDEX idx_flights_airline_id ON flights (airline_id);
CREATE INDEX idx_flights_origin_airport_id ON flights (origin_airport_id);
CREATE INDEX idx_flights_destination_airport_id ON flights (destination_airport_id);
CREATE INDEX idx_flights_status ON flights (status);

CREATE TABLE flight_segments
(
    id                     UUID                     NOT NULL,
    flight_id              UUID                     NOT NULL,
    sequence               INTEGER                  NOT NULL,
    origin_airport_id      UUID                     NOT NULL,
    destination_airport_id UUID                     NOT NULL,
    departure_time         TIMESTAMP WITH TIME ZONE NOT NULL,
    arrival_time           TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_flight_segments PRIMARY KEY (id),
    CONSTRAINT fk_flight_segments_flight FOREIGN KEY (flight_id) REFERENCES flights (id)
);

CREATE INDEX idx_flight_segments_flight_id ON flight_segments (flight_id);
