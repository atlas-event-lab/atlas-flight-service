package com.atlas.flight.event;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Money representation inside catalog event payloads (flight-events.yaml Money). */
public record MoneyEvent(@NotNull BigDecimal amount, String currency) {}
