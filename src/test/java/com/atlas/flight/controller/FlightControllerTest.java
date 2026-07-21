package com.atlas.flight.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.atlas.flight.config.SecurityConfig;
import com.atlas.flight.dto.CreateFlightRequest;
import com.atlas.flight.dto.FlightListResponse;
import com.atlas.flight.dto.UpdateFlightRequest;
import com.atlas.flight.exception.CapacityBelowReservedException;
import com.atlas.flight.exception.DuplicateFlightException;
import com.atlas.flight.exception.FlightNotFoundException;
import com.atlas.flight.exception.InventoryUnavailableException;
import com.atlas.flight.service.FlightService;
import com.atlas.flight.support.FlightTestData;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Tracer;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(FlightController.class)
@Import(SecurityConfig.class)
class FlightControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    FlightService flightService;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @MockitoBean
    Tracer tracer;

    private static final String BASE_URL = "/api/v1/flights";

    private static RequestPostProcessor adminJwt() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Test
    void createFlight_valid_returns201() throws Exception {
        when(flightService.createFlight(any(CreateFlightRequest.class))).thenReturn(FlightTestData.aFlightResponse());

        mvc.perform(post(BASE_URL)
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(FlightTestData.aCreateFlightRequest())))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flightId").value(FlightTestData.FLIGHT_ID.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createFlight_invalidBody_returns400() throws Exception {
        String invalidBody = "{\"flightNumber\":\"\"}";

        mvc.perform(post(BASE_URL)
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void createFlight_duplicate_returns409() throws Exception {
        when(flightService.createFlight(any(CreateFlightRequest.class)))
                .thenThrow(new DuplicateFlightException(FlightTestData.FLIGHT_NUMBER, FlightTestData.DEPARTURE));

        mvc.perform(post(BASE_URL)
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(FlightTestData.aCreateFlightRequest())))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void createFlight_unauthenticated_returns401() throws Exception {
        mvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(FlightTestData.aCreateFlightRequest())))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /flights ───────────────────────────────────────────────────────────

    @Test
    void listFlights_returns200() throws Exception {
        when(flightService.listFlights(any()))
                .thenReturn(new FlightListResponse(List.of(FlightTestData.aFlightResponse()), 0, 20, 1, 1));

        mvc.perform(get(BASE_URL).with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].flightId").value(FlightTestData.FLIGHT_ID.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getFlight_found_returns200() throws Exception {
        when(flightService.getFlight(FlightTestData.FLIGHT_ID)).thenReturn(FlightTestData.aFlightResponse());

        mvc.perform(get(BASE_URL + "/{flightId}", FlightTestData.FLIGHT_ID).with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightId").value(FlightTestData.FLIGHT_ID.toString()));
    }

    @Test
    void getFlight_notFound_returns404() throws Exception {
        when(flightService.getFlight(FlightTestData.FLIGHT_ID))
                .thenThrow(new FlightNotFoundException(FlightTestData.FLIGHT_ID));

        mvc.perform(get(BASE_URL + "/{flightId}", FlightTestData.FLIGHT_ID).with(adminJwt()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    // ── PUT /flights/{id} ──────────────────────────────────────────────────────

    @Test
    void updateFlight_capacityBelowReserved_returns409() throws Exception {
        when(flightService.updateFlight(eq(FlightTestData.FLIGHT_ID), any(UpdateFlightRequest.class)))
                .thenThrow(new CapacityBelowReservedException(FlightTestData.FLIGHT_ID, 100, 150));

        mvc.perform(put(BASE_URL + "/{flightId}", FlightTestData.FLIGHT_ID)
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(FlightTestData.anUpdateFlightRequest(100))))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void updateFlight_inventoryUnavailable_returns503() throws Exception {
        when(flightService.updateFlight(eq(FlightTestData.FLIGHT_ID), any(UpdateFlightRequest.class)))
                .thenThrow(new InventoryUnavailableException(FlightTestData.FLIGHT_ID, new RuntimeException("down")));

        mvc.perform(put(BASE_URL + "/{flightId}", FlightTestData.FLIGHT_ID)
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(FlightTestData.anUpdateFlightRequest(100))))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    // ── DELETE /flights/{id} ───────────────────────────────────────────────────

    @Test
    void withdrawFlight_returns204() throws Exception {
        doNothing().when(flightService).withdrawFlight(FlightTestData.FLIGHT_ID);

        mvc.perform(delete(BASE_URL + "/{flightId}", FlightTestData.FLIGHT_ID).with(adminJwt()))
                .andExpect(status().isNoContent());
    }

    @Test
    void withdrawFlight_notFound_returns404() throws Exception {
        doThrow(new FlightNotFoundException(FlightTestData.FLIGHT_ID))
                .when(flightService)
                .withdrawFlight(FlightTestData.FLIGHT_ID);

        mvc.perform(delete(BASE_URL + "/{flightId}", FlightTestData.FLIGHT_ID).with(adminJwt()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }
}
