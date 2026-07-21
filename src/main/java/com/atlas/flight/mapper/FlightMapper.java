package com.atlas.flight.mapper;

import com.atlas.flight.dto.FlightResponse;
import com.atlas.flight.dto.FlightSegmentResponse;
import com.atlas.flight.dto.MoneyResponse;
import com.atlas.flight.entity.Flight;
import com.atlas.flight.entity.FlightSegment;
import com.atlas.flight.entity.Money;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps Flight entities to API response DTOs (mapping is a service-layer concern). */
@Mapper(componentModel = "spring")
public interface FlightMapper {

    @Mapping(target = "flightId", source = "id")
    FlightResponse toResponse(Flight flight);

    MoneyResponse toMoneyResponse(Money money);

    FlightSegmentResponse toSegmentResponse(FlightSegment segment);

    List<FlightSegmentResponse> toSegmentResponses(List<FlightSegment> segments);
}
