package com.nexturn.vehicleregistration.service.impl;

import static com.nexturn.vehicleregistration.service.WorkflowSupport.ensure;
import com.nexturn.vehicleregistration.dto.request.PaymentRequest;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;
import com.nexturn.vehicleregistration.entity.OwnerPaymentDetail;
import com.nexturn.vehicleregistration.entity.VehicleRegistrationApplication;
import com.nexturn.vehicleregistration.enums.ApplicationStatus;
import com.nexturn.vehicleregistration.enums.PaymentStatus;
import com.nexturn.vehicleregistration.exception.ApplicationIdNotFoundException;
import com.nexturn.vehicleregistration.exception.PaymentAmountMismatchException;
import com.nexturn.vehicleregistration.repository.ApplicationWorkflowRepository;
import com.nexturn.vehicleregistration.repository.OwnerPaymentRepository;
import com.nexturn.vehicleregistration.service.ApplicationQueryService;
import com.nexturn.vehicleregistration.service.AuditService;
import com.nexturn.vehicleregistration.service.PaymentService;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Records a simulated successful payment once per application under a database lock; does not charge money. */
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private ApplicationWorkflowRepository applicationRepository;

    @Autowired
    private OwnerPaymentRepository paymentRepository;

    @Autowired
    private ApplicationQueryService applicationQueryService;

    @Autowired
    private AuditService auditService;

    @Override
    public ApplicationDetailsResponse pay(
            String referenceNumber,
            PaymentRequest request) {
        VehicleRegistrationApplication application = applicationRepository
                .locked(referenceNumber)
                .orElseThrow(
                        () -> new ApplicationIdNotFoundException(referenceNumber));

        ensure(
                application.getApplicationStatus() != ApplicationStatus.REJECTED
                        && application.getApplicationStatus()
                                != ApplicationStatus.APPROVED,
                "Application is closed");

        if (application.getPayableAmount().compareTo(request.amount()) != 0) {
            throw new PaymentAmountMismatchException();
        }

        // Retrying the same payment returns the stored result without creating another payment.
        if (paymentRepository
                .findByApplicationApplicationRefNo(referenceNumber)
                .isPresent()) {
            return applicationQueryService.detail(application);
        }

        OwnerPaymentDetail payment = new OwnerPaymentDetail();

        payment.setApplication(application);
        payment.setAmountPaid(application.getPayableAmount());
        payment.setCardLastFourDigit(request.cardLastFourDigit());
        payment.setTransactionReferenceId("SIM-" + UUID.randomUUID());
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(Instant.now());

        paymentRepository.save(payment);

        auditService.record(
                application,
                "PAYMENT_RECORDED",
                "Simulated payment");

        return applicationQueryService.detail(application);
    }
}
