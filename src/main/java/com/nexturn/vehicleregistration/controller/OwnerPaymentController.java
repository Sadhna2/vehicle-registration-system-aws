package com.nexturn.vehicleregistration.controller;

import com.nexturn.vehicleregistration.dto.request.PaymentRequest;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;
import com.nexturn.vehicleregistration.service.VehicleRegistrationApplicationService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Exposes simulated payment recording for a vehicle registration application. */
@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@RequestMapping("/api/applications")
public class OwnerPaymentController {
    @Autowired
    private VehicleRegistrationApplicationService applicationService;

    /** Records the original payment simulation after validating the quoted fee. */
    @Operation(summary = "Record simulated registration payment", description = "No real charge is made. Supply only the amount and four card digits.")
    @PostMapping("/{ref}/payments")
    public ApplicationDetailsResponse pay(@PathVariable("ref") String referenceNumber,
            @Valid @RequestBody PaymentRequest request) {
        return applicationService.pay(referenceNumber, request);
    }
}
