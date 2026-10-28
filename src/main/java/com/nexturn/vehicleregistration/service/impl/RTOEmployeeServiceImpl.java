package com.nexturn.vehicleregistration.service.impl;

import static com.nexturn.vehicleregistration.service.WorkflowSupport.ensure;
import com.nexturn.vehicleregistration.dto.request.AccountRequest;
import com.nexturn.vehicleregistration.dto.request.RTOEmployeeRequest;
import com.nexturn.vehicleregistration.dto.response.OwnerResponse;
import com.nexturn.vehicleregistration.dto.response.RTOEmployeeResponse;
import com.nexturn.vehicleregistration.entity.Owner;
import com.nexturn.vehicleregistration.entity.RTOEmployee;
import com.nexturn.vehicleregistration.exception.DuplicateRecordException;
import com.nexturn.vehicleregistration.exception.EmployeeNotFoundException;
import com.nexturn.vehicleregistration.exception.InvalidRequestException;
import com.nexturn.vehicleregistration.exception.OwnerNotFoundException;
import com.nexturn.vehicleregistration.repository.OwnerRepository;
import com.nexturn.vehicleregistration.repository.RTOEmployeeRepository;
import com.nexturn.vehicleregistration.service.AuditService;
import com.nexturn.vehicleregistration.service.RTOEmployeeService;
import com.nexturn.vehicleregistration.util.PasswordUtil;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Manages employees and account status without adding JWT, authentication filters or authorization checks. */
@Service
@Transactional
public class RTOEmployeeServiceImpl implements RTOEmployeeService {
    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private RTOEmployeeRepository employeeRepository;

    @Autowired
    private AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public List<RTOEmployeeResponse> employees() {
        return employeeRepository.findAll()
                .stream()
                .map(this::toEmployeeResponse)
                .toList();
    }

    @Override
    public RTOEmployeeResponse employee(
            RTOEmployeeRequest request) {
        String email = request.emailAddress()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (employeeRepository.findByEmailAddress(email).isPresent()) {
            throw new DuplicateRecordException(
                    "Employee email is already registered");
        }

        RTOEmployee employee = new RTOEmployee();

        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmailAddress(email);
        employee.setPassword(PasswordUtil.hash(request.password()));
        employee.setPhoneNumber(request.phoneNumber());
        employee.setDesignation(request.designation());
        employee.setRole(request.role());

        RTOEmployee savedEmployee = employeeRepository.save(employee);

        auditService.record(
                null,
                "EMPLOYEE_CREATED",
                savedEmployee.getEmailAddress());

        return toEmployeeResponse(savedEmployee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OwnerResponse> owners() {
        return ownerRepository.findAll()
                .stream()
                .map(this::toOwnerResponse)
                .toList();
    }

    @Override
    public void account(
            String accountType,
            Long accountId,
            AccountRequest request) {
        if ("owners".equals(accountType)) {
            updateOwner(accountId, request);

        } else if ("employees".equals(accountType)) {
            updateEmployee(accountId, request);

        } else {
            throw new InvalidRequestException("Unknown account type");
        }

        auditService.record(
                null,
                "ACCOUNT_UPDATED",
                accountType + ":" + accountId);
    }

    private void updateOwner(Long ownerId, AccountRequest request) {
        ensure(
                request.role() == null,
                "Owners have no database role field");

        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new OwnerNotFoundException(ownerId));

        owner.setStatus(request.status());
        owner.setDateOfUpdate(Instant.now());
    }

    private void updateEmployee(
            Long employeeId,
            AccountRequest request) {
        RTOEmployee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        if (request.role() != null) {
            employee.setRole(request.role());
        }

        employee.setStatus(request.status());
    }

    private OwnerResponse toOwnerResponse(Owner owner) {
                if (owner == null) {
                        return null;
                }

                return new OwnerResponse(
                                owner.getOwnerId(),
                                owner.getFirstName(),
                                owner.getLastName(),
                                owner.getEmailAddress(),
                                owner.getPhoneNumber(),
                                owner.getDateOfBirth(),
                                owner.getIdentityProofType(),
                                owner.getIdentityProofNumber(),
                                owner.getAddress(),
                                owner.getCityName(),
                                owner.getStateName(),
                                owner.getPincode(),
                                owner.getStatus(),
                                owner.getDateOfCreation(),
                                owner.getDateOfUpdate()
                );
        }

    private RTOEmployeeResponse toEmployeeResponse(RTOEmployee employee) {
                if (employee == null) {
                        return null;
                }

                return new RTOEmployeeResponse(
                                employee.getEmployeeId(),
                                employee.getFirstName(),
                                employee.getLastName(),
                                employee.getEmailAddress(),
                                employee.getPhoneNumber(),
                                employee.getDesignation(),
                                employee.getRole(),
                                employee.getStatus(),
                                employee.getDateOfJoining()
                );
        }
}
