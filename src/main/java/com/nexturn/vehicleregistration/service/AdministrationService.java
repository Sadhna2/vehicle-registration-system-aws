package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.dto.request.AccountRequest;
import com.nexturn.vehicleregistration.dto.request.RTOEmployeeRequest;
import com.nexturn.vehicleregistration.dto.request.RegistrationFeeRuleRequest;
import com.nexturn.vehicleregistration.dto.response.AuditLogResponse;
import com.nexturn.vehicleregistration.dto.response.OwnerResponse;
import com.nexturn.vehicleregistration.dto.response.RTOEmployeeResponse;
import com.nexturn.vehicleregistration.dto.response.RegistrationFeeRuleResponse;

import java.util.List;
import java.util.Map;

/** Groups existing administrative features while delegating each concern to its own service. */
public interface AdministrationService {
    List<RegistrationFeeRuleResponse> fees();

    RegistrationFeeRuleResponse fee(
            RegistrationFeeRuleRequest request);

    List<RTOEmployeeResponse> employees();

    RTOEmployeeResponse employee(
            RTOEmployeeRequest request);

    List<OwnerResponse> owners();

    void account(
            String accountType,
            Long accountId,
            AccountRequest request);

    Map<String, Object> report();

    List<AuditLogResponse> audits();
}
