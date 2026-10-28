package com.nexturn.vehicleregistration.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexturn.vehicleregistration.entity.RegistrationFeeRule;
import com.nexturn.vehicleregistration.enums.ApplicationType;
import com.nexturn.vehicleregistration.enums.VehicleCategory;

/** Finds the latest fee effective on the date of submission. */
public interface RegistrationFeeRuleRepository extends JpaRepository<RegistrationFeeRule, Long> {
	Optional<RegistrationFeeRule> findFirstByVehicleCategoryAndApplicationTypeAndEffectiveFromLessThanEqualOrderByEffectiveFromDescFeeRuleIdDesc(
			VehicleCategory category, ApplicationType type, LocalDate today);
}
