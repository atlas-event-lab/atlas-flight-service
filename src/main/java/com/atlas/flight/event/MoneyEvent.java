package com.atlas.flight.event;

import java.math.BigDecimal;

/** Money representation inside catalog event payloads (flight-events.yaml Money). */
public record MoneyEvent(BigDecimal amount, String currency) {}
