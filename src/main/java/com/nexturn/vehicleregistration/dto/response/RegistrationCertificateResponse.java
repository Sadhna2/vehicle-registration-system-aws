package com.nexturn.vehicleregistration.dto.response;

import java.time.LocalDate;

/** Returns the RC number, issuing employee, registered owner and certificate validity dates. */
public record RegistrationCertificateResponse(
        String registrationNumber,
        String applicationRefNo,
        Long issuedByEmployeeId,
        String issuedByEmployeeName,
        LocalDate issuedDate,
        LocalDate validTill,
        Long registeredOwnerId,
        String registeredOwnerName
) {
}
