package com.nexturn.vehicleregistration.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.nexturn.vehicleregistration.entity.Owner;

/**
 * Provides JPA persistence and normalized email lookup for owner accounts.
 */
public interface OwnerRepository extends JpaRepository<Owner, Long> {
    /**
     * Looks up an owner using the normalized email address.
     * @param email normalized email address
     * @return the matching account, or an empty Optional
     */
    Optional<Owner> findByEmailAddress(String email);

}
