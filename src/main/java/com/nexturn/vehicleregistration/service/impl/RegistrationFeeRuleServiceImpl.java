package com.nexturn.vehicleregistration.service.impl;

import static com.nexturn.vehicleregistration.service.WorkflowSupport.ensure;
import com.nexturn.vehicleregistration.dto.request.RegistrationFeeRuleRequest;
import com.nexturn.vehicleregistration.dto.response.RegistrationFeeRuleResponse;
import com.nexturn.vehicleregistration.entity.RegistrationFeeRule;
import com.nexturn.vehicleregistration.enums.ApplicationType;
import com.nexturn.vehicleregistration.repository.RegistrationFeeRuleRepository;
import com.nexturn.vehicleregistration.service.AuditService;
import com.nexturn.vehicleregistration.service.RegistrationFeeRuleService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Configures only NEW registration fees and records the change in the audit log. */
@Service
@Transactional
public class RegistrationFeeRuleServiceImpl
        implements RegistrationFeeRuleService {
    @Autowired
    private RegistrationFeeRuleRepository feeRuleRepository;

    @Autowired
    private AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationFeeRuleResponse> fees() {
        return feeRuleRepository
                .findAll(Sort.by("effectiveFrom").descending())
                .stream()
                .filter(rule -> rule.getApplicationType() == ApplicationType.NEW)
                .map(this::toFeeResponse)
                .toList();
    }

    @Override
    public RegistrationFeeRuleResponse fee(
            RegistrationFeeRuleRequest request) {
        ensure(
                request.applicationType() == ApplicationType.NEW,
                "Only new vehicle registration fee rules are supported");

        RegistrationFeeRule feeRule = new RegistrationFeeRule();

        feeRule.setVehicleCategory(request.vehicleCategory());
        feeRule.setApplicationType(ApplicationType.NEW);
        feeRule.setFeeAmount(request.feeAmount());
        feeRule.setEffectiveFrom(request.effectiveFrom());

        RegistrationFeeRule savedFeeRule = feeRuleRepository.save(feeRule);

        auditService.record(
                null,
                "FEE_CREATED",
                ApplicationType.NEW.name());

        return toFeeResponse(savedFeeRule);
    }

    private RegistrationFeeRuleResponse toFeeResponse(RegistrationFeeRule f) {
        return new RegistrationFeeRuleResponse(
                f.getFeeRuleId(),
                f.getVehicleCategory(),
                f.getApplicationType(),
                f.getFeeAmount(),
                f.getEffectiveFrom());
    }
}
