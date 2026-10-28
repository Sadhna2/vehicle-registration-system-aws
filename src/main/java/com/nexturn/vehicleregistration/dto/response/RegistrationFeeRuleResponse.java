package com.nexturn.vehicleregistration.dto.response;

import com.nexturn.vehicleregistration.enums.ApplicationType;
import com.nexturn.vehicleregistration.enums.VehicleCategory;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Returns the configured fee amount, category and effective date. */
public record RegistrationFeeRuleResponse(
    Long feeRuleId,
    VehicleCategory vehicleCategory,
    ApplicationType applicationType,
    BigDecimal feeAmount,
    LocalDate effectiveFrom) {}
