package com.nexturn.vehicleregistration.dto.request;
import java.time.LocalDate;

import com.nexturn.vehicleregistration.enums.IdentityProofType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Carries owner registration fields and validation constraints.
 * The controller validates this request through @Valid.
 * Contains a plain-text password and must not be logged.
 *
 * @param firstName owner's first name
 * @param lastName owner's last name
 * @param emailAddress email used for registration and login
 * @param phoneNumber contact number
 * @param password plain-text input to hash before persistence
 * @param dateOfBirth owner's date of birth
 * @param identityProofType accepted identity proof type
 * @param identityProofNumber unique identity proof number
 * @param address residential address
 * @param cityName city of residence
 * @param stateName state of residence
 * @param pincode six-digit postal code
 */
public record OwnerRequest(

		@NotBlank @Size(max = 30) String firstName,
		@NotBlank @Size(max = 30) String lastName,
		@NotBlank @Email @Size(max = 100) String emailAddress,
		@NotBlank @Pattern(regexp = "[0-9+]{10,15}") String phoneNumber,
		@NotBlank @Size(min = 10, max = 128) String password,
		@NotNull @Past LocalDate dateOfBirth,
		@NotNull IdentityProofType identityProofType,
		@NotBlank @Size(max = 30) String identityProofNumber,
		@NotBlank @Size(max = 150) String address,
		@NotBlank @Size(max = 50) String cityName,
		@NotBlank @Size(max = 50) String stateName,
		@NotBlank @Pattern(regexp = "[0-9]{6}") String pincode
) {
}
