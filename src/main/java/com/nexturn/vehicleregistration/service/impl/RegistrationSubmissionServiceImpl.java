package com.nexturn.vehicleregistration.service.impl;

import static com.nexturn.vehicleregistration.service.WorkflowSupport.ensure;
import com.nexturn.vehicleregistration.dto.request.NewVehicleRegistrationRequest;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;
import com.nexturn.vehicleregistration.entity.Owner;
import com.nexturn.vehicleregistration.entity.RegistrationFeeRule;
import com.nexturn.vehicleregistration.entity.Vehicle;
import com.nexturn.vehicleregistration.entity.VehicleRegistrationApplication;
import com.nexturn.vehicleregistration.enums.ApplicationStatus;
import com.nexturn.vehicleregistration.enums.ApplicationType;
import com.nexturn.vehicleregistration.exception.ApplicationIdNotFoundException;
import com.nexturn.vehicleregistration.exception.FeeRuleNotFoundException;
import com.nexturn.vehicleregistration.exception.OwnerNotFoundException;
import com.nexturn.vehicleregistration.repository.ApplicationWorkflowRepository;
import com.nexturn.vehicleregistration.repository.OwnerRepository;
import com.nexturn.vehicleregistration.repository.RegistrationFeeRuleRepository;
import com.nexturn.vehicleregistration.repository.VechileRepository;
import com.nexturn.vehicleregistration.service.ApplicationQueryService;
import com.nexturn.vehicleregistration.service.AuditService;
import com.nexturn.vehicleregistration.service.RegistrationSubmissionService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Saves the owner vehicle, fee quotation, reference number and audit entry in one transaction. */
@Service
@Transactional
public class RegistrationSubmissionServiceImpl
        implements RegistrationSubmissionService {
    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private VechileRepository vehicleRepository;

    @Autowired
    private ApplicationWorkflowRepository applicationRepository;

    @Autowired
    private RegistrationFeeRuleRepository feeRuleRepository;

    @Autowired
    private ApplicationQueryService applicationQueryService;

    @Autowired
    private AuditService auditService;

    @Override
    public ApplicationDetailsResponse submit(
            NewVehicleRegistrationRequest request, Long ownerId) {
        Owner owner = ownerRepository
                .findById(ownerId)
                .orElseThrow(() -> new OwnerNotFoundException(ownerId));

        Vehicle vehicle = new Vehicle();
        vehicle.setCurrentOwner(owner);

        updateVehicle(vehicle, request);

        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        VehicleRegistrationApplication application =
                createApplication(savedVehicle, owner);

        return applicationQueryService.detail(application);
    }

    @Override
    public ApplicationDetailsResponse correct(
            String referenceNumber,
            NewVehicleRegistrationRequest request) {
        VehicleRegistrationApplication application = applicationRepository
                .locked(referenceNumber)
                .orElseThrow(
                        () -> new ApplicationIdNotFoundException(referenceNumber));

        ensure(
                application.getApplicationStatus()
                        == ApplicationStatus.CORRECTION_REQUIRED,
                "Only applications awaiting correction can be edited");

        ensure(
                application.getVehicle().getVehicleCategory()
                        == request.vehicleCategory(),
                "Vehicle category cannot change after fee quotation");

        updateVehicle(application.getVehicle(), request);

        application.setApplicationStatus(ApplicationStatus.SUBMITTED);
        application.setUpdatedDate(Instant.now());

        auditService.record(
                application, "RESUBMITTED", null);

        return applicationQueryService.detail(application);
    }

    /** Selects the effective fee before creating the application and submission audit entry. */
    private VehicleRegistrationApplication createApplication(
            Vehicle vehicle,
            Owner owner) {
        boolean openApplicationExists = applicationRepository
                .existsByVehicleTemporaryregisterNoAndApplicationStatusNotIn(
                        vehicle.getTemporaryregisterNo(),
                        List.of(
                                ApplicationStatus.APPROVED,
                                ApplicationStatus.REJECTED));

        ensure(
                !openApplicationExists,
                "Vehicle already has an open application");

        RegistrationFeeRule feeRule = feeRuleRepository
                .findFirstByVehicleCategoryAndApplicationTypeAndEffectiveFromLessThanEqualOrderByEffectiveFromDescFeeRuleIdDesc(
                        vehicle.getVehicleCategory(),
                        ApplicationType.NEW,
                        LocalDate.now())
                .orElseThrow(FeeRuleNotFoundException::new);

        String referenceNumber = "VRS-"
                + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 24)
                        .toUpperCase(Locale.ROOT);

        VehicleRegistrationApplication application =
                new VehicleRegistrationApplication();

        application.setApplicationRefNo(referenceNumber);
        application.setVehicle(vehicle);
        application.setApplicant(owner);
        application.setApplicationType(ApplicationType.NEW);
        application.setFeeRule(feeRule);
        application.setPayableAmount(feeRule.getFeeAmount());

        VehicleRegistrationApplication savedApplication =
                applicationRepository.save(application);

        auditService.record(
                savedApplication,
                "SUBMITTED",
                ApplicationType.NEW.name());

        return savedApplication;
    }

    /** Rejects future manufacture years and normalizes unique chassis and engine identifiers. */
    private void updateVehicle(
            Vehicle vehicle,
            NewVehicleRegistrationRequest request) {
        ensure(
                request.manufactureYear() <= Year.now().getValue(),
                "Manufacture year cannot be in the future");

        vehicle.setVehicleCategory(request.vehicleCategory());
        vehicle.setManufacturerName(request.manufacturerName());
        vehicle.setModelName(request.modelName());

        vehicle.setChassisNumber(
                request.chassisNumber().trim().toUpperCase(Locale.ROOT));

        vehicle.setEngineNumber(
                request.engineNumber().trim().toUpperCase(Locale.ROOT));

        vehicle.setFuelType(request.fuelType());
        vehicle.setManufactureYear(request.manufactureYear());
        vehicle.setColorVariant(request.colorVariant());
    }
}
