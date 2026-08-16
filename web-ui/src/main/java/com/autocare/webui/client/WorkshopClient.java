package com.autocare.webui.client;

import com.autocare.webui.dto.JobDto;
import com.autocare.webui.dto.MechanicDto;
import com.autocare.webui.dto.SpecialtyDto;
import com.autocare.webui.dto.WorkshopStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WorkshopClient {

    private static final Logger log = LoggerFactory.getLogger(WorkshopClient.class);

    private final RestClient client;

    public WorkshopClient(RestClient workshopRestClient) {
        this.client = workshopRestClient;
    }

    public List<JobDto> findJobs(String status) {
        try {
            String uri = (status == null || status.isBlank())
                    ? "/api/workshop/jobs"
                    : "/api/workshop/jobs?status=" + status;
            List<JobDto> result = client.get().uri(uri).retrieve()
                    .body(new ParameterizedTypeReference<List<JobDto>>() { });
            return result == null ? List.of() : result;
        } catch (Exception ex) {
            log.warn("workshop-service unavailable: {}", ex.getMessage());
            return List.of();
        }
    }

    public JobDto findJob(Long id) {
        try {
            return client.get().uri("/api/workshop/jobs/{id}", id).retrieve().body(JobDto.class);
        } catch (Exception ex) {
            log.warn("Could not load job {}: {}", id, ex.getMessage());
            return null;
        }
    }

    public List<MechanicDto> findMechanics() {
        try {
            List<MechanicDto> result = client.get().uri("/api/workshop/mechanics").retrieve()
                    .body(new ParameterizedTypeReference<List<MechanicDto>>() { });
            return result == null ? List.of() : result;
        } catch (Exception ex) {
            log.warn("workshop-service unavailable: {}", ex.getMessage());
            return List.of();
        }
    }

    public List<SpecialtyDto> findSpecialties() {
        try {
            List<SpecialtyDto> result = client.get().uri("/api/workshop/specialties").retrieve()
                    .body(new ParameterizedTypeReference<List<SpecialtyDto>>() { });
            return result == null ? List.of() : result;
        } catch (Exception ex) {
            return List.of();
        }
    }

    public WorkshopStats stats() {
        try {
            WorkshopStats stats = client.get().uri("/api/workshop/jobs/stats").retrieve()
                    .body(WorkshopStats.class);
            return stats == null ? WorkshopStats.empty() : stats;
        } catch (Exception ex) {
            log.warn("workshop-service unavailable: {}", ex.getMessage());
            return WorkshopStats.empty();
        }
    }

    public void createJob(Long vehicleId, Long mechanicId, String description, BigDecimal cost) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("vehicleId", vehicleId);
        payload.put("mechanicId", mechanicId);
        payload.put("description", description);
        payload.put("estimatedCost", cost);
        payload.put("status", "RECEIVED");

        client.post().uri("/api/workshop/jobs")
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }

    public void updateStatus(Long jobId, String status) {
        client.patch().uri("/api/workshop/jobs/{id}/status?status={status}", jobId, status)
                .retrieve()
                .toBodilessEntity();
    }

    public boolean isUp() {
        try {
            client.get().uri("/actuator/health").retrieve().toBodilessEntity();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
