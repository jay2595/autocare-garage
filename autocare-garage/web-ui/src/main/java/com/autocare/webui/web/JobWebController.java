package com.autocare.webui.web;

import com.autocare.webui.client.CustomersClient;
import com.autocare.webui.client.WorkshopClient;
import com.autocare.webui.dto.JobDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class JobWebController {

    private static final List<String> STATUSES =
            List.of("RECEIVED", "DIAGNOSING", "IN_PROGRESS", "COMPLETED", "DELIVERED");

    private final WorkshopClient workshop;
    private final CustomersClient customers;

    public JobWebController(WorkshopClient workshop, CustomersClient customers) {
        this.workshop = workshop;
        this.customers = customers;
    }

    @GetMapping("/jobs")
    public String list(@RequestParam(required = false) String status, Model model) {
        model.addAttribute("jobs", workshop.findJobs(status));
        model.addAttribute("statuses", STATUSES);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("stats", workshop.stats());
        model.addAttribute("active", "jobs");
        return "jobs/list";
    }

    @GetMapping("/jobs/new")
    public String newForm(Model model) {
        model.addAttribute("vehicles", customers.findAllVehicles());
        model.addAttribute("mechanics", workshop.findMechanics());
        model.addAttribute("active", "jobs");
        return "jobs/form";
    }

    @PostMapping("/jobs")
    public String create(@RequestParam Long vehicleId,
                         @RequestParam(required = false) Long mechanicId,
                         @RequestParam String description,
                         @RequestParam(required = false) BigDecimal estimatedCost,
                         RedirectAttributes redirect) {
        try {
            workshop.createJob(vehicleId, mechanicId, description, estimatedCost);
            redirect.addFlashAttribute("message", "Job booked in");
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", "Could not book job: " + ex.getMessage());
        }
        return "redirect:/jobs";
    }

    @GetMapping("/jobs/{id}")
    public String detail(@PathVariable Long id, Model model) {
        JobDto job = workshop.findJob(id);
        if (job == null) {
            model.addAttribute("active", "jobs");
            model.addAttribute("missing", "Job " + id);
            return "not-found";
        }
        model.addAttribute("job", job);
        model.addAttribute("statuses", STATUSES);
        model.addAttribute("active", "jobs");
        return "jobs/detail";
    }

    @PostMapping("/jobs/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               RedirectAttributes redirect) {
        try {
            workshop.updateStatus(id, status);
            redirect.addFlashAttribute("message", "Job #" + id + " moved to " + status);
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", "Could not update job: " + ex.getMessage());
        }
        return "redirect:/jobs/" + id;
    }
}
