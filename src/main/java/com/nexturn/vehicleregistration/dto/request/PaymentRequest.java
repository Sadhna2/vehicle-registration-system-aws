package com.nexturn.vehicleregistration.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** Validates the quoted amount and four card digits for a simulated payment; accepts no full card number or CVV. */
public record PaymentRequest(

        @NotNull
        @DecimalMin("0.01")
        BigDecimal amount,
        @NotNull
        @Pattern(regexp = "[0-9]{4}")
        String cardLastFourDigit

) {
}
