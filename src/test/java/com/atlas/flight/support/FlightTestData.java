package com.atlas.flight.support;

import com.atlas.flight.dto.CreateFlightRequest;
import com.atlas.flight.dto.FlightResponse;
import com.atlas.flight.dto.MoneyRequest;
import com.atlas.flight.dto.MoneyResponse;
import com.atlas.flight.dto.UpdateFlightRequest;
import com.atlas.flight.entity.Flight;
import com.atlas.flight.entity.FlightStatus;
import com.atlas.flight.entity.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Test Data Builders for the flight catalog (coding-standards §Unit Tests). */
public final class FlightTestData {

    public static final UUID FLIGHT_ID = UUID.fromString("c1111111-1111-1111-1111-111111111111");
    public static final UUID AIRLINE_ID = UUID.fromString("a1111111-1111-1111-1111-111111111111");
    public static final UUID ORIGIN_ID = UUID.fromString("b1111111-1111-1111-1111-111111111111");
    public static final UUID DEST_ID = UUID.fromString("b2222222-2222-2222-2222-222222222222");

    public static final String FLIGHT_NUMBER = "AT100";
    public static final Instant DEPARTURE = Instant.parse("2026-07-01T08:00:00Z");
    public static final Instant ARRIVAL = Instant.parse("2026-07-01T23:00:00Z");
    public static final int TOTAL_SEATS = 180;

    private FlightTestData() {}

    public static MoneyRequest aMoneyRequest() {
        return new MoneyRequest(new BigDecimal("1200.00"), "USD");
    }

    public static CreateFlightRequest aCreateFlightRequest() {
        return new CreateFlightRequest(
                FLIGHT_NUMBER,
                AIRLINE_ID,
                ORIGIN_ID,
                DEST_ID,
                DEPARTURE,
                ARRIVAL,
                TOTAL_SEATS,
                aMoneyRequest(),
                List.of());
    }

    public static UpdateFlightRequest anUpdateFlightRequest(int totalSeats) {
        return new UpdateFlightRequest(
                FLIGHT_NUMBER,
                AIRLINE_ID,
                ORIGIN_ID,
                DEST_ID,
                DEPARTURE,
                ARRIVAL,
                totalSeats,
                aMoneyRequest(),
                List.of());
    }

    public static Flight aFlight() {
        return new Flight(
                FLIGHT_ID,
                FLIGHT_NUMBER,
                AIRLINE_ID,
                ORIGIN_ID,
                DEST_ID,
                DEPARTURE,
                ARRIVAL,
                TOTAL_SEATS,
                new Money(new BigDecimal("1200.00"), "USD"),
                FlightStatus.ACTIVE);
    }

    public static FlightResponse aFlightResponse() {
        return new FlightResponse(
                FLIGHT_ID,
                FLIGHT_NUMBER,
                AIRLINE_ID,
                ORIGIN_ID,
                DEST_ID,
                DEPARTURE,
                ARRIVAL,
                TOTAL_SEATS,
                new MoneyResponse(new BigDecimal("1200.00"), "USD"),
                FlightStatus.ACTIVE,
                List.of());
    }
}
