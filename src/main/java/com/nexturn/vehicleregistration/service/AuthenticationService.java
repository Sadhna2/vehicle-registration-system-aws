package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.dto.response.LoginResponse;
import com.nexturn.vehicleregistration.dto.request.LoginRequest;
import com.nexturn.vehicleregistration.dto.request.OwnerRequest;

/**
 * Defines owner signup and owner or RTO employee credential verification.
 * Returns account details without issuing authentication tokens.
 */
public interface AuthenticationService {
    /**
     * Registers an active owner and stores a password hash.
     * @param request validated owner details
     * @return the registered owner account details
     */
    LoginResponse signup(OwnerRequest request);

    /**
     * Verifies credentials and rejects inactive accounts.
     * @param request validated credentials and account selector
     * @return owner or employee account details
     */
    LoginResponse login(LoginRequest request);
}
