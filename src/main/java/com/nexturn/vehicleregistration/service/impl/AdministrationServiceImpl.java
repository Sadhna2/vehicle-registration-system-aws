package com.nexturn.vehicleregistration.service.impl;

import com.nexturn.vehicleregistration.dto.request.AccountRequest;
import com.nexturn.vehicleregistration.dto.request.RTOEmployeeRequest;
import com.nexturn.vehicleregistration.dto.request.RegistrationFeeRuleRequest;
import com.nexturn.vehicleregistration.dto.response.AuditLogResponse;
import com.nexturn.vehicleregistration.dto.response.OwnerResponse;
import com.nexturn.vehicleregistration.dto.response.RTOEmployeeResponse;
import com.nexturn.vehicleregistration.dto.response.RegistrationFeeRuleResponse;
import com.nexturn.vehicleregistration.service.AdministrationService;
import com.nexturn.vehicleregistration.service.RTOEmployeeService;
import com.nexturn.vehicleregistration.service.RegistrationFeeRuleService;
import com.nexturn.vehicleregistration.service.ReportService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Delegates fee, employee/account and reporting operations to separate service implementations. */
@Service
public class AdministrationServiceImpl implements AdministrationService {
    @Autowired
    private RegistrationFeeRuleService registrationFeeRuleService;

    @Autowired
    private RTOEmployeeService employeeService;

    @Autowired
    private ReportService reportService;

    @Override
    public List<RegistrationFeeRuleResponse> fees() {
        return registrationFeeRuleService.fees();
    }

    @Override
    public RegistrationFeeRuleResponse fee(
            RegistrationFeeRuleRequest request) {
        return registrationFeeRuleService.fee(request);
    }

    @Override
    public List<RTOEmployeeResponse> employees() {
        return employeeService.employees();
    }

    @Override
    public RTOEmployeeResponse employee(
            RTOEmployeeRequest request) {
        return employeeService.employee(request);
    }

    @Override
    public List<OwnerResponse> owners() {
        return employeeService.owners();
    }

    @Override
    public void account(
            String accountType,
            Long accountId,
            AccountRequest request) {
        employeeService.account(accountType, accountId, request);
    }

    @Override
    public Map<String, Object> report() {
        return reportService.report();
    }

    @Override
    public List<AuditLogResponse> audits() {
        return reportService.audits();
    }
}
