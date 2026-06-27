package com.atlas.flight.controller;

import com.atlas.flight.config.SecurityConfig;
import com.atlas.flight.dto.FlightPriceResponse;
import com.atlas.flight.dto.MoneyResponse;
import com.atlas.flight.entity.FlightStatus;
import com.atlas.flight.exception.FlightNotFoundException;
import com.atlas.flight.service.FlightService;
import com.atlas.flight.support.FlightTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FlightPriceController.class)
@Import(SecurityConfig.class)
class FlightPriceControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean FlightService flightService;
    @MockitoBean JwtDecoder jwtDecoder;

    private static final String PRICE_URL = "/api/v1/flights/{flightId}/price";

    @Test
    void getFlightPrice_authenticated_returns200() throws Exception {
        FlightPriceResponse price = new FlightPriceResponse(
                FlightTestData.FLIGHT_ID,
                new MoneyResponse(new BigDecimal("1200.00"), "USD"),
                FlightStatus.ACTIVE);
        when(flightService.getFlightPrice(FlightTestData.FLIGHT_ID)).thenReturn(price);

        mvc.perform(get(PRICE_URL, FlightTestData.FLIGHT_ID).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flightId").value(FlightTestData.FLIGHT_ID.toString()))
                .andExpect(jsonPath("$.basePrice.amount").value(1200.00))
                .andExpect(jsonPath("$.basePrice.currency").value("USD"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getFlightPrice_withdrawnFlight_returnsWithdrawnStatus() throws Exception {
        FlightPriceResponse price = new FlightPriceResponse(
                FlightTestData.FLIGHT_ID,
                new MoneyResponse(new BigDecimal("1200.00"), "USD"),
                FlightStatus.WITHDRAWN);
        when(flightService.getFlightPrice(FlightTestData.FLIGHT_ID)).thenReturn(price);

        mvc.perform(get(PRICE_URL, FlightTestData.FLIGHT_ID).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WITHDRAWN"));
    }

    @Test
    void getFlightPrice_notFound_returns404() throws Exception {
        when(flightService.getFlightPrice(FlightTestData.FLIGHT_ID))
                .thenThrow(new FlightNotFoundException(FlightTestData.FLIGHT_ID));

        mvc.perform(get(PRICE_URL, FlightTestData.FLIGHT_ID).with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void getFlightPrice_nonAdminUser_returns200() throws Exception {
        FlightPriceResponse price = new FlightPriceResponse(
                FlightTestData.FLIGHT_ID,
                new MoneyResponse(new BigDecimal("1200.00"), "USD"),
                FlightStatus.ACTIVE);
        when(flightService.getFlightPrice(FlightTestData.FLIGHT_ID)).thenReturn(price);

        mvc.perform(get(PRICE_URL, FlightTestData.FLIGHT_ID).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightId").value(FlightTestData.FLIGHT_ID.toString()));
    }
}
