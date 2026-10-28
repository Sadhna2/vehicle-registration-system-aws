package com.nexturn.vehicleregistration.repository;

import java.util.List;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import com.nexturn.vehicleregistration.entity.Vehicle;

/** Preserves the previous repository name and queries vehicles by their current owner. */
public interface VechileRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByCurrentOwnerOwnerId(Long id);
    /** Counts registered vehicles whose validity is within the existing report deadline. */
    long countByRegistrationValidTillLessThanEqual(LocalDate deadline);
}
