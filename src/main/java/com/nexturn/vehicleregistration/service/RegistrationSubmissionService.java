package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.dto.request.NewVehicleRegistrationRequest;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;

/** Defines atomic new-vehicle submission and correction operations. */
public interface RegistrationSubmissionService {
    /** Creates a NEW application and returns its unique tracking reference. */
    ApplicationDetailsResponse submit(
            NewVehicleRegistrationRequest request, Long ownerId);

    /** Resubmits corrected details only when the application requires correction. */
    ApplicationDetailsResponse correct(
            String referenceNumber,
            NewVehicleRegistrationRequest request);
}
