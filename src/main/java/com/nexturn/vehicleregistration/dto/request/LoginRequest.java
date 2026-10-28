package com.nexturn.vehicleregistration.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Carries login credentials and the account selector.
 * staff=true selects an employee; staff=false selects an owner.
 * Contains a plain-text password and must not be logged.
 *
 * @param email registered email address
 * @param password password to verify
 * @param staff true for employee login, false for owner login
 */
public record LoginRequest(

		@NotBlank @Email String email,
		@NotBlank String password,
		@NotNull Boolean staff) {
}
