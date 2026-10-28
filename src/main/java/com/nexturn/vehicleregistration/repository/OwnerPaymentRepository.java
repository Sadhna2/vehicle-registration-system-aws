package com.nexturn.vehicleregistration.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nexturn.vehicleregistration.entity.OwnerPaymentDetail;
import com.nexturn.vehicleregistration.enums.PaymentStatus;

/** Finds the single payment for an application and totals successful simulated payments. */
public interface OwnerPaymentRepository extends JpaRepository<OwnerPaymentDetail, Long> {
	Optional<OwnerPaymentDetail> findByApplicationApplicationRefNo(String ref);

	@Query("""
			SELECT COALESCE(SUM(p.amountPaid), 0)
			FROM OwnerPaymentDetail p
			WHERE p.paymentStatus = :status
			""")
	BigDecimal collected(@Param("status") PaymentStatus status);
}
