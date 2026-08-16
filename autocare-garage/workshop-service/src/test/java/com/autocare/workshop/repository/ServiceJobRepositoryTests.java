package com.autocare.workshop.repository;

import com.autocare.workshop.model.JobStatus;
import com.autocare.workshop.model.ServiceJob;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ServiceJobRepositoryTests {

    @Autowired
    private ServiceJobRepository jobs;

    private ServiceJob job(Long vehicleId, JobStatus status) {
        ServiceJob j = new ServiceJob();
        j.setVehicleId(vehicleId);
        j.setDescription("Brake inspection");
        j.setStatus(status);
        return j;
    }

    @Test
    void findsJobsByStatus() {
        jobs.save(job(1L, JobStatus.RECEIVED));
        jobs.save(job(2L, JobStatus.COMPLETED));

        assertThat(jobs.findByStatusOrderByCreatedAtDesc(JobStatus.RECEIVED)).hasSize(1);
    }

    @Test
    void findsJobsByVehicle() {
        jobs.save(job(7L, JobStatus.RECEIVED));
        jobs.save(job(7L, JobStatus.IN_PROGRESS));
        jobs.save(job(8L, JobStatus.RECEIVED));

        assertThat(jobs.findByVehicleIdOrderByCreatedAtDesc(7L)).hasSize(2);
    }

    @Test
    void countsByStatus() {
        jobs.save(job(1L, JobStatus.IN_PROGRESS));
        jobs.save(job(2L, JobStatus.IN_PROGRESS));

        assertThat(jobs.countByStatus(JobStatus.IN_PROGRESS)).isEqualTo(2);
        assertThat(jobs.countByStatus(JobStatus.DELIVERED)).isZero();
    }
}
