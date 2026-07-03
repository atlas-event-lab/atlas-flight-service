
CREATE TABLE airlines
(
    id         UUID         NOT NULL,
    iata_code  VARCHAR(3)   NOT NULL,
    name       VARCHAR(255) NOT NULL,
    country    VARCHAR(255) NOT NULL,
    CONSTRAINT pk_airlines PRIMARY KEY (id),
    CONSTRAINT uq_airlines_iata_code UNIQUE (iata_code)
);

CREATE TABLE airports
(
    id         UUID         NOT NULL,
    iata_code  VARCHAR(3)   NOT NULL,
    name       VARCHAR(255) NOT NULL,
    city       VARCHAR(255) NOT NULL,
    country    VARCHAR(255) NOT NULL,
    CONSTRAINT pk_airports PRIMARY KEY (id),
    CONSTRAINT uq_airports_iata_code UNIQUE (iata_code)
);
