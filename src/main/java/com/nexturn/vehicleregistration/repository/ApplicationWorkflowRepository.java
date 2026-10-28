package com.nexturn.vehicleregistration.repository;

import com.nexturn.vehicleregistration.entity.VehicleRegistrationApplication;
import com.nexturn.vehicleregistration.enums.ApplicationStatus;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Queries NEW applications and locks records during correction; contains no transfer query. */
public interface ApplicationWorkflowRepository
    extends JpaRepository<VehicleRegistrationApplication, String> {
  List<VehicleRegistrationApplication> findByApplicantOwnerId(Long id, Sort sort);

  boolean existsByVehicleTemporaryregisterNoAndApplicationStatusNotIn(
      Long id, Collection<ApplicationStatus> statuses);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select a from VehicleRegistrationApplication a where a.applicationRefNo = :ref")
  Optional<VehicleRegistrationApplication> locked(@Param("ref") String ref);

  interface StatusCount {
    ApplicationStatus getStatus();

    long getTotal();
  }

  @Query(
      """
      select a.applicationStatus as status, count(a) as total
      from VehicleRegistrationApplication a
      group by a.applicationStatus
      """)
  List<StatusCount> countStatuses();
}
