package com.autocare.workshop.web;

import com.autocare.workshop.client.CustomersClient;
import com.autocare.workshop.client.VehicleRef;
import com.autocare.workshop.model.JobStatus;
import com.autocare.workshop.model.Mechanic;
import com.autocare.workshop.model.ServiceJob;
import com.autocare.workshop.repository.MechanicRepository;
import com.autocare.workshop.repository.ServiceJobRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workshop/jobs")
public class ServiceJobController {

    private final ServiceJobRepository jobs;
    private final MechanicRepository mechanics;
    private final CustomersClient customersClient;

    public ServiceJobController(ServiceJobRepository jobs,
                                MechanicRepository mechanics,
                                CustomersClient customersClient) {
        this.jobs = jobs;
        this.mechanics = mechanics;
        this.customersClient = customersClient;
    }

    @GetMapping
    public List<JobView> list(@RequestParam(required = false) JobStatus status,
                              @RequestParam(required = false) Long vehicleId) {
        List<ServiceJob> found;
        if (status != null) {
            found = jobs.findByStatusOrderByCreatedAtDesc(status);
        } else if (vehicleId != null) {
            found = jobs.findByVehicleIdOrderByCreatedAtDesc(vehicleId);
        } else {
            found = jobs.findAllByOrderByCreatedAtDesc();
        }

        List<JobView> views = new ArrayList<>();
        for (ServiceJob job : found) {
            views.add(toView(job));
        }
        return views;
    }

    @GetMapping("/{id}")
    public JobView get(@PathVariable Long id) {
        return toView(jobs.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service job", id)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobView create(@Valid @RequestBody ServiceJob job) {
        job.setId(null);
        if (job.getStatus() == null) {
            job.setStatus(JobStatus.RECEIVED);
        }
        return toView(jobs.save(job));
    }

    @PatchMapping("/{id}/status")
    public JobView updateStatus(@PathVariable Long id, @RequestParam JobStatus status) {
        ServiceJob job = jobs.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service job", id));
        job.transitionTo(status);
        return toView(jobs.save(job));
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        Map<String, Long> byStatus = new LinkedHashMap<>();
        long open = 0;
        for (JobStatus status : JobStatus.values()) {
            long count = jobs.countByStatus(status);
            byStatus.put(status.name(), count);
            if (status.isOpen()) {
                open += count;
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", jobs.count());
        result.put("open", open);
        result.put("byStatus", byStatus);
        result.put("customersServiceReachable", customersClient.isReachable());
        return result;
    }

    private JobView toView(ServiceJob job) {
        VehicleRef vehicle = customersClient.findVehicle(job.getVehicleId());

        String mechanicName = null;
        if (job.getMechanicId() != null) {
            mechanicName = mechanics.findById(job.getMechanicId())
                    .map(Mechanic::getFullName)
                    .orElse(null);
        }

        return new JobView(
                job.getId(),
                job.getDescription(),
                job.getStatus(),
                job.getStatus().getLabel(),
                job.getEstimatedCost(),
                job.getCreatedAt(),
                job.getCompletedAt(),
                job.getVehicleId(),
                vehicle,
                job.getMechanicId(),
                mechanicName);
    }
}
