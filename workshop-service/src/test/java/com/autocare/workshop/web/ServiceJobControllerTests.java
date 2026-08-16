package com.autocare.workshop.web;

import com.autocare.workshop.client.CustomersClient;
import com.autocare.workshop.client.VehicleRef;
import com.autocare.workshop.model.JobStatus;
import com.autocare.workshop.model.ServiceJob;
import com.autocare.workshop.repository.MechanicRepository;
import com.autocare.workshop.repository.ServiceJobRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServiceJobController.class)
@ActiveProfiles("test")
class ServiceJobControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceJobRepository jobs;

    @MockBean
    private MechanicRepository mechanics;

    @MockBean
    private CustomersClient customersClient;

    private ServiceJob sampleJob() {
        ServiceJob job = new ServiceJob();
        job.setId(1L);
        job.setVehicleId(5L);
        job.setDescription("Front brake pads");
        job.setStatus(JobStatus.IN_PROGRESS);
        return job;
    }

    @Test
    void enrichesJobWithVehicleFromCustomersService() throws Exception {
        given(jobs.findAllByOrderByCreatedAtDesc()).willReturn(List.of(sampleJob()));
        given(customersClient.findVehicle(5L)).willReturn(
                new VehicleRef(5L, "Toyota", "Corolla", 2018, "OSK4821", 87650, 3L, "Mei Tanaka"));

        mockMvc.perform(get("/api/workshop/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vehicle.make").value("Toyota"))
                .andExpect(jsonPath("$[0].statusLabel").value("In progress"));
    }

    @Test
    void stillReturnsJobWhenCustomersServiceIsDown() throws Exception {
        given(jobs.findAllByOrderByCreatedAtDesc()).willReturn(List.of(sampleJob()));
        given(customersClient.findVehicle(anyLong())).willReturn(null);

        mockMvc.perform(get("/api/workshop/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].vehicle").doesNotExist());
    }

    @Test
    void returns404ForUnknownJob() throws Exception {
        given(jobs.findById(anyLong())).willReturn(Optional.empty());

        mockMvc.perform(get("/api/workshop/jobs/404"))
                .andExpect(status().isNotFound());
    }
}
