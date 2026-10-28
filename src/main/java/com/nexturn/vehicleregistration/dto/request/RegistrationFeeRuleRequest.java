package com.nexturn.vehicleregistration.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.nexturn.vehicleregistration.enums.ApplicationType;
import com.nexturn.vehicleregistration.enums.VehicleCategory;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/** Validates the fee configuration required before new-vehicle submission. */
public record RegistrationFeeRuleRequest(

        @NotNull
        VehicleCategory vehicleCategory,
        @NotNull
        ApplicationType applicationType,
        @NotNull
        @DecimalMin("0.01")
        BigDecimal feeAmount,
        @NotNull
        LocalDate effectiveFrom

) {
}
