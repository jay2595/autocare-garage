package com.autocare.webui.web;

import com.autocare.webui.client.CustomersClient;
import com.autocare.webui.client.WorkshopClient;
import com.autocare.webui.dto.CustomerDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CustomerWebController {

    private final CustomersClient customers;
    private final WorkshopClient workshop;

    public CustomerWebController(CustomersClient customers, WorkshopClient workshop) {
        this.customers = customers;
        this.workshop = workshop;
    }

    @GetMapping("/customers")
    public String list(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("customers", customers.findAll(q));
        model.addAttribute("q", q);
        model.addAttribute("active", "customers");
        return "customers/list";
    }

    @GetMapping("/customers/new")
    public String newForm(Model model) {
        model.addAttribute("active", "customers");
        return "customers/form";
    }

    @PostMapping("/customers")
    public String create(@RequestParam String firstName,
                         @RequestParam String lastName,
                         @RequestParam String phone,
                         @RequestParam(required = false) String email,
                         @RequestParam(required = false) String address,
                         @RequestParam(required = false) String city,
                         RedirectAttributes redirect) {
        try {
            customers.create(firstName, lastName, phone, email, address, city);
            redirect.addFlashAttribute("message", lastName + " added to the customer list");
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", "Could not save customer: " + ex.getMessage());
        }
        return "redirect:/customers";
    }

    @GetMapping("/customers/{id}")
    public String detail(@PathVariable Long id, Model model) {
        CustomerDto customer = customers.findById(id);
        if (customer == null) {
            model.addAttribute("active", "customers");
            model.addAttribute("missing", "Customer " + id);
            return "not-found";
        }
        model.addAttribute("customer", customer);
        model.addAttribute("jobs", workshop.findJobs(null).stream()
                .filter(j -> j.vehicle() != null && id.equals(j.vehicle().customerId()))
                .toList());
        model.addAttribute("active", "customers");
        return "customers/detail";
    }
}
