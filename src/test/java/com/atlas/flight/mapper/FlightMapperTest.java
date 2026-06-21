package com.atlas.flight.mapper;

import com.atlas.flight.dto.FlightResponse;
import com.atlas.flight.entity.Flight;
import com.atlas.flight.entity.FlightSegment;
import com.atlas.flight.support.FlightTestData;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FlightMapperTest {

    private final FlightMapper mapper = new FlightMapperImpl();

    @Test
    void toResponse_maps_id_to_flightId_money_and_segments() {
        Flight flight = FlightTestData.aFlight();
        flight.addSegment(new FlightSegment(
                UUID.randomUUID(), 1, FlightTestData.ORIGIN_ID, FlightTestData.DEST_ID,
                FlightTestData.DEPARTURE, FlightTestData.ARRIVAL));

        FlightResponse response = mapper.toResponse(flight);

        assertThat(response.flightId()).isEqualTo(FlightTestData.FLIGHT_ID);
        assertThat(response.flightNumber()).isEqualTo(FlightTestData.FLIGHT_NUMBER);
        assertThat(response.basePrice().currency()).isEqualTo("USD");
        assertThat(response.totalSeats()).isEqualTo(FlightTestData.TOTAL_SEATS);
        assertThat(response.segments()).hasSize(1);
        assertThat(response.segments().get(0).sequence()).isEqualTo(1);
    }
}
