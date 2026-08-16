package com.autocare.workshop.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceJobTests {

    @Test
    void stampsCompletionTimeWhenJobCloses() {
        ServiceJob job = new ServiceJob();
        job.setStatus(JobStatus.IN_PROGRESS);

        job.transitionTo(JobStatus.COMPLETED);

        assertThat(job.getStatus()).isEqualTo(JobStatus.COMPLETED);
        assertThat(job.getCompletedAt()).isNotNull();
    }

    @Test
    void clearsCompletionTimeWhenJobReopens() {
        ServiceJob job = new ServiceJob();
        job.transitionTo(JobStatus.COMPLETED);

        job.transitionTo(JobStatus.IN_PROGRESS);

        assertThat(job.getCompletedAt()).isNull();
    }

    @Test
    void openStatusesAreOpen() {
        assertThat(JobStatus.RECEIVED.isOpen()).isTrue();
        assertThat(JobStatus.DIAGNOSING.isOpen()).isTrue();
        assertThat(JobStatus.IN_PROGRESS.isOpen()).isTrue();
        assertThat(JobStatus.COMPLETED.isOpen()).isFalse();
        assertThat(JobStatus.DELIVERED.isOpen()).isFalse();
    }
}
