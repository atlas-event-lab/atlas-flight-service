package com.atlas.flight.dto;

import java.math.BigDecimal;

/** Money representation in API responses (common/money.yaml). */
public record MoneyResponse(BigDecimal amount, String currency) {}
