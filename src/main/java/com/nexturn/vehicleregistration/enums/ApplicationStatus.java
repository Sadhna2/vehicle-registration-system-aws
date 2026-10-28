package com.nexturn.vehicleregistration.enums;

/** Preserves the ApplicationStatus values from the previous backend database model. */
public enum ApplicationStatus {
	SUBMITTED,
    UNDER_VERIFICATION,
    CORRECTION_REQUIRED,
    INSPECTION_PENDING,
    APPROVED,
    REJECTED
}
