package com.nexturn.vehicleregistration.controller;

import com.nexturn.vehicleregistration.dto.response.RegistrationCertificateResponse;
import com.nexturn.vehicleregistration.service.VehicleRegistrationApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Returns certificate data as JSON after RC issuance; does not upload or download documents. */
@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@RequestMapping("/api/applications")
public class RegistrationCertificateController {
    @Autowired
    private VehicleRegistrationApplicationService applicationService;

    /** Returns the issued RC fields for an approved application. */
    @GetMapping("/{ref}/certificate")
    public RegistrationCertificateResponse certificate(@PathVariable("ref") String referenceNumber) {
        return applicationService.certificate(referenceNumber);
    }
}
