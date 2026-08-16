package com.autocare.webui.web;

import com.autocare.webui.client.CustomersClient;
import com.autocare.webui.client.WorkshopClient;
import com.autocare.webui.dto.CustomerDto;
import com.autocare.webui.dto.JobDto;
import com.autocare.webui.dto.WorkshopStats;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final CustomersClient customers;
    private final WorkshopClient workshop;

    public DashboardController(CustomersClient customers, WorkshopClient workshop) {
        this.customers = customers;
        this.workshop = workshop;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        WorkshopStats stats = workshop.stats();
        List<JobDto> jobs = workshop.findJobs(null);
        List<CustomerDto> allCustomers = customers.findAll(null);

        int vehicleCount = 0;
        for (CustomerDto c : allCustomers) {
            vehicleCount += c.vehicleCount();
        }

        model.addAttribute("stats", stats);
        model.addAttribute("recentJobs", jobs.size() > 6 ? jobs.subList(0, 6) : jobs);
        model.addAttribute("customerCount", allCustomers.size());
        model.addAttribute("vehicleCount", vehicleCount);
        model.addAttribute("mechanicCount", workshop.findMechanics().size());
        model.addAttribute("customersUp", !allCustomers.isEmpty() || customers.isUp());
        model.addAttribute("workshopUp", workshop.isUp());
        model.addAttribute("active", "dashboard");
        return "index";
    }

    @GetMapping("/status")
    public String status(Model model) {
        model.addAttribute("customersUp", customers.isUp());
        model.addAttribute("workshopUp", workshop.isUp());
        model.addAttribute("stats", workshop.stats());
        model.addAttribute("active", "status");
        return "status";
    }
}
