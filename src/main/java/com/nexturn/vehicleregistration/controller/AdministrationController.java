package com.nexturn.vehicleregistration.controller;

import com.nexturn.vehicleregistration.dto.request.AccountRequest;
import com.nexturn.vehicleregistration.dto.request.RTOEmployeeRequest;
import com.nexturn.vehicleregistration.dto.response.AuditLogResponse;
import com.nexturn.vehicleregistration.dto.response.OwnerResponse;
import com.nexturn.vehicleregistration.dto.response.RTOEmployeeResponse;
import com.nexturn.vehicleregistration.service.AdministrationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Exposes the previous backend employee/account, report and audit endpoints. */
@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@RequestMapping("/api")
public class AdministrationController {
    @Autowired
    private AdministrationService administrationService;

    @GetMapping("/reports")
    public Map<String,Object> report() { return administrationService.report(); }

    @GetMapping("/audit")
    public List<AuditLogResponse> audit() { return administrationService.audits(); }

    @GetMapping("/employees")
    public List<RTOEmployeeResponse> employees() { return administrationService.employees(); }

    @PostMapping("/employees")
    public RTOEmployeeResponse employee(@Valid @RequestBody RTOEmployeeRequest request) {
        return administrationService.employee(request);
    }

    @GetMapping("/owners")
    public List<OwnerResponse> owners() { return administrationService.owners(); }

    @PatchMapping("/accounts/{kind}/{id}")
    public void account(@PathVariable("kind") String accountType, @PathVariable("id") Long accountId,
            @Valid @RequestBody AccountRequest request) {
        administrationService.account(accountType, accountId, request);
    }
}
