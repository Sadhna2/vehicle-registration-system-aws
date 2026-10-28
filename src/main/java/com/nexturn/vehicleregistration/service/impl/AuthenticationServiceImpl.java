package com.nexturn.vehicleregistration.service.impl;

import com.nexturn.vehicleregistration.dto.request.LoginRequest;
import com.nexturn.vehicleregistration.dto.request.OwnerRequest;
import com.nexturn.vehicleregistration.dto.response.LoginResponse;
import com.nexturn.vehicleregistration.entity.Owner;
import com.nexturn.vehicleregistration.entity.RTOEmployee;
import com.nexturn.vehicleregistration.enums.AccountStatus;
import com.nexturn.vehicleregistration.exception.DuplicateRecordException;
import com.nexturn.vehicleregistration.exception.InvalidLoginException;
import com.nexturn.vehicleregistration.repository.OwnerRepository;
import com.nexturn.vehicleregistration.repository.RTOEmployeeRepository;
import com.nexturn.vehicleregistration.service.AuthenticationService;
import com.nexturn.vehicleregistration.util.PasswordUtil;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registers owners and verifies owner or RTO employee credentials.
 * Normalizes emails and preserves the existing password hash format.
 * Read operations are read-only; signup uses a write transaction.
 */
@Service
@Transactional(readOnly = true)
public class AuthenticationServiceImpl implements AuthenticationService {
    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private RTOEmployeeRepository employeeRepository;

    @Override
    @Transactional
    public LoginResponse signup(OwnerRequest request) {
        String email = request.emailAddress()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (ownerRepository.findByEmailAddress(email).isPresent()) {
            throw new DuplicateRecordException(
                    "Owner email is already registered");
        }

        Owner owner = new Owner();

        owner.setFirstName(request.firstName());
        owner.setLastName(request.lastName());
        owner.setEmailAddress(email);
        owner.setPhoneNumber(request.phoneNumber());
        owner.setPassword(PasswordUtil.hash(request.password()));
        owner.setDateOfBirth(request.dateOfBirth());
        owner.setIdentityProofType(request.identityProofType());
        owner.setIdentityProofNumber(request.identityProofNumber());
        owner.setAddress(request.address());
        owner.setCityName(request.cityName());
        owner.setStateName(request.stateName());
        owner.setPincode(request.pincode());

        Owner savedOwner = ownerRepository.save(owner);

        return new LoginResponse(
                savedOwner.getOwnerId(),
                savedOwner.getFirstName(),
                "OWNER");
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (request.staff()) {
            return loginEmployee(email, request.password());
        }

        return loginOwner(email, request.password());
    }

    /** Verifies an active employee and returns the stored role. */
    private LoginResponse loginEmployee(String email, String password) {
        RTOEmployee employee = employeeRepository
                .findByEmailAddress(email)
                .orElseThrow(this::invalidLogin);

        if (employee.getStatus() != AccountStatus.ACTIVE
                || !PasswordUtil.matches(password, employee.getPassword())) {
            throw invalidLogin();
        }

        return new LoginResponse(
                employee.getEmployeeId(),
                employee.getFirstName(),
                employee.getRole().name());
    }

    /** Verifies an active owner and returns the OWNER response label. */
    private LoginResponse loginOwner(String email, String password) {
        Owner owner = ownerRepository
                .findByEmailAddress(email)
                .orElseThrow(this::invalidLogin);

        if (owner.getStatus() != AccountStatus.ACTIVE
                || !PasswordUtil.matches(password, owner.getPassword())) {
            throw invalidLogin();
        }

        return new LoginResponse(
                owner.getOwnerId(),
                owner.getFirstName(),
                "OWNER");
    }

    /** Keeps login errors consistent for unknown, inactive and incorrect accounts. */
    private InvalidLoginException invalidLogin() {
        return new InvalidLoginException(
                "Invalid credentials or inactive account");
    }
}
