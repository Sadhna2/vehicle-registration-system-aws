package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.dto.request.AccountRequest;
import com.nexturn.vehicleregistration.dto.request.RTOEmployeeRequest;
import com.nexturn.vehicleregistration.dto.response.OwnerResponse;
import com.nexturn.vehicleregistration.dto.response.RTOEmployeeResponse;

import java.util.List;

/** Defines employee creation, account maintenance and owner/employee queries. */
public interface RTOEmployeeService {
    List<RTOEmployeeResponse> employees();

    RTOEmployeeResponse employee(
            RTOEmployeeRequest request);

    List<OwnerResponse> owners();

    void account(
            String accountType,
            Long accountId,
            AccountRequest request);
}
