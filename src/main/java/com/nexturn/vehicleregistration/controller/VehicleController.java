package com.nexturn.vehicleregistration.controller;

import com.nexturn.vehicleregistration.dto.response.VehicleResponse;
import com.nexturn.vehicleregistration.service.VehicleRegistrationApplicationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Exposes vehicle queries, optionally filtered by the owner ID. */
@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@RequestMapping("/api/vehicles")
public class VehicleController {
    @Autowired
    private VehicleRegistrationApplicationService applicationService;

    @GetMapping
    public List<VehicleResponse> vehicles(@RequestParam(name="ownerId", required=false) Long ownerId) {
        return applicationService.vehicles(ownerId);
    }
}
