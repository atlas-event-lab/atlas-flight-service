package com.atlas.flight.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

/**
 * Money in API requests (common/money.yaml). Amounts use BigDecimal and are non-negative;
 * currency is an ISO-4217 code (domain/money.md).
 */
public record MoneyRequest(

        @NotNull
        @DecimalMin(value = "0.0", message = "amount must be non-negative")
        BigDecimal amount,

        @NotNull
        @Pattern(regexp = "[A-Z]{3}", message = "currency must be a 3-letter ISO-4217 code")
        String currency
) {}
