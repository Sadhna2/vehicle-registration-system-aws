package com.nexturn.vehicleregistration.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.nexturn.vehicleregistration.enums.ApplicationStatus;
import com.nexturn.vehicleregistration.enums.ApplicationType;

/** Returns application reference, status and summary fields for the dashboard. */
public record ApplicationSummaryResponse(String reference,
		ApplicationType type,
		ApplicationStatus status,
		BigDecimal amount,
		Instant submittedAt,
		Instant updatedAt,
		Long vehicleId,
		String vehicle,
		String owner
) {
}
