package com.nexturn.vehicleregistration.controller;

import java.util.List;
import com.nexturn.vehicleregistration.dto.request.ApplicationReviewRequest;
import com.nexturn.vehicleregistration.dto.request.NewVehicleRegistrationRequest;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;
import com.nexturn.vehicleregistration.dto.response.ApplicationSummaryResponse;
import com.nexturn.vehicleregistration.service.VehicleRegistrationApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Exposes submission, correction, tracking and RTO review for new-vehicle applications. */
@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@RequestMapping("/api/applications")
public class VehicleRegistrationApplicationController {
    @Autowired
    private VehicleRegistrationApplicationService applicationService;

    @GetMapping
    public List<ApplicationSummaryResponse> list(
            @RequestParam(name = "ownerId", required = false) Long ownerId) {
        return applicationService.list(ownerId);
    }

    @PostMapping
    public ApplicationDetailsResponse submit(@RequestParam("ownerId") Long ownerId,
            @Valid @RequestBody NewVehicleRegistrationRequest request) {
        return applicationService.submit(request, ownerId);
    }

    @GetMapping("/{ref}")
    public ApplicationDetailsResponse get(@PathVariable("ref") String referenceNumber) {
        return applicationService.get(referenceNumber);
    }

    @PutMapping("/{ref}/correction")
    public ApplicationDetailsResponse correct(@PathVariable("ref") String referenceNumber,
            @Valid @RequestBody NewVehicleRegistrationRequest request) {
        return applicationService.correct(referenceNumber, request);
    }

    /** Applies START, CORRECTION, VERIFY, SCHEDULE, PASS, FAIL, APPROVE or REJECT. */
    @PostMapping("/{ref}/review")
    public ApplicationDetailsResponse review(@PathVariable("ref") String referenceNumber,
            @RequestParam("employeeId") Long employeeId,
            @Valid @RequestBody ApplicationReviewRequest request) {
        return applicationService.review(referenceNumber, request, employeeId);
    }
}
