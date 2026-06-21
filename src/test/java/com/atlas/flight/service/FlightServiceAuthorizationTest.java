package com.atlas.flight.service;

import com.atlas.flight.client.InventoryClient;
import com.atlas.flight.event.FlightEventPayloadFactory;
import com.atlas.flight.mapper.FlightMapper;
import com.atlas.flight.messaging.OutboxEventWriter;
import com.atlas.flight.repository.AirlineRepository;
import com.atlas.flight.repository.AirportRepository;
import com.atlas.flight.repository.FlightRepository;
import com.atlas.flight.support.FlightTestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Verifies that RBAC {@code ADMIN} is enforced inside the business service via
 * {@code @PreAuthorize} (SEC-004). Loads only the service + method-security advisors so the
 * proxy is active; all collaborators are mocked.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FlightServiceImpl.class, FlightServiceAuthorizationTest.MethodSecurityConfig.class})
class FlightServiceAuthorizationTest {

    @Configuration
    @EnableMethodSecurity
    static class MethodSecurityConfig {}

    @MockitoBean FlightRepository flightRepository;
    @MockitoBean AirlineRepository airlineRepository;
    @MockitoBean AirportRepository airportRepository;
    @MockitoBean InventoryClient inventoryClient;
    @MockitoBean OutboxEventWriter outboxEventWriter;
    @MockitoBean FlightEventPayloadFactory payloadFactory;
    @MockitoBean FlightMapper flightMapper;

    @Autowired
    FlightService flightService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminRole_canInvoke() {
        when(flightRepository.findById(any())).thenReturn(Optional.of(FlightTestData.aFlight()));
        when(flightMapper.toResponse(any())).thenReturn(FlightTestData.aFlightResponse());

        assertThat(flightService.getFlight(FlightTestData.FLIGHT_ID)).isNotNull();
    }

    @Test
    @WithMockUser(roles = "USER")
    void nonAdminRole_isForbidden() {
        assertThatThrownBy(() -> flightService.getFlight(FlightTestData.FLIGHT_ID))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithAnonymousUser
    void anonymous_isForbidden() {
        assertThatThrownBy(() -> flightService.getFlight(FlightTestData.FLIGHT_ID))
                .isInstanceOfAny(AccessDeniedException.class, AuthenticationCredentialsNotFoundException.class);
    }
}
