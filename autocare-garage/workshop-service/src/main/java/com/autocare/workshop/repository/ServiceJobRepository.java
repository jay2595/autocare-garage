package com.autocare.workshop.repository;

import com.autocare.workshop.model.JobStatus;
import com.autocare.workshop.model.ServiceJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceJobRepository extends JpaRepository<ServiceJob, Long> {

    List<ServiceJob> findByStatusOrderByCreatedAtDesc(JobStatus status);

    List<ServiceJob> findByVehicleIdOrderByCreatedAtDesc(Long vehicleId);

    List<ServiceJob> findAllByOrderByCreatedAtDesc();

    long countByStatus(JobStatus status);
}
