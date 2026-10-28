package com.nexturn.vehicleregistration.dto.response;

import java.time.LocalDate;

import com.nexturn.vehicleregistration.enums.FuelType;
import com.nexturn.vehicleregistration.enums.VehicleCategory;

/** Returns vehicle fields and its current owner using the previous backend contract. */
public record VehicleResponse(
        Long temporaryregisterNo,
        OwnerResponse currentOwner,
        VehicleCategory vehicleCategory,
        String manufacturerName,
        String modelName,
        String chassisNumber,
        String engineNumber,
        FuelType fuelType,
        int manufactureYear,
        String registrationcertificateNumber,
        LocalDate firstRegistrationDate,
        LocalDate registrationValidTill,
        String colorVariant
) {
}
