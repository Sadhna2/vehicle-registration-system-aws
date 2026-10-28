package com.nexturn.vehicleregistration.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Carries a review action, remarks and optional inspection appointment from the RTO workflow. */
public record ApplicationReviewRequest(

        @NotBlank
        String action,
        @Size(max = 500)
        String remarks,
        LocalDate appointment

) {
}
