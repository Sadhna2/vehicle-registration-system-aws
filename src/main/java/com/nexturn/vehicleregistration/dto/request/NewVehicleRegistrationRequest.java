package com.nexturn.vehicleregistration.dto.request;

import com.nexturn.vehicleregistration.enums.FuelType;
import com.nexturn.vehicleregistration.enums.VehicleCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Validates the vehicle fields accepted when submitting or correcting an application. */
public record NewVehicleRegistrationRequest(
    @NotNull VehicleCategory vehicleCategory,
    @NotBlank @Size(max = 50) String manufacturerName,
    @NotBlank @Size(max = 50) String modelName,
    @NotBlank @Size(max = 50) String chassisNumber,
    @NotBlank @Size(max = 50) String engineNumber,
    @NotNull FuelType fuelType,
    @Min(1900) int manufactureYear,
    @Size(max = 30) String colorVariant) {}
