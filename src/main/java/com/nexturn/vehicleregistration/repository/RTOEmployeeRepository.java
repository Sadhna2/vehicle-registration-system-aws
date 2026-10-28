package com.nexturn.vehicleregistration.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.nexturn.vehicleregistration.entity.RTOEmployee;

/**
 * Provides JPA persistence and normalized email lookup for employee login.
 */
public interface RTOEmployeeRepository extends JpaRepository<RTOEmployee, Long> {
    /**
     * Looks up an employee using the normalized email address.
     * @param email normalized email address
     * @return the matching account, or an empty Optional
     */
	Optional<RTOEmployee> findByEmailAddress(String email);

}
