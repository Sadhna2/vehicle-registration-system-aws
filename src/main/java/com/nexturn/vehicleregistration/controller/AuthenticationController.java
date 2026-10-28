package com.nexturn.vehicleregistration.controller;

import com.nexturn.vehicleregistration.dto.request.LoginRequest;
import com.nexturn.vehicleregistration.dto.request.OwnerRequest;
import com.nexturn.vehicleregistration.dto.response.LoginResponse;
import com.nexturn.vehicleregistration.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes validated owner signup and owner or employee login endpoints.
 * Delegates business rules to AuthenticationService and returns response DTOs.
 */
@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@RequestMapping("/api/auth")

public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;

    /** Registers an owner from validated request fields. */
    @PostMapping("/signup")
    public LoginResponse signup(@Valid @RequestBody OwnerRequest request) {
        return authenticationService.signup(request);
    }

    /** Verifies credentials and returns account details for the frontend. */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticationService.login(request);
    }
}
