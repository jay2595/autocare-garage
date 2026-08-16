package com.autocare.webui.web;

import com.autocare.webui.client.WorkshopClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MechanicWebController {

    private final WorkshopClient workshop;

    public MechanicWebController(WorkshopClient workshop) {
        this.workshop = workshop;
    }

    @GetMapping("/mechanics")
    public String list(Model model) {
        model.addAttribute("mechanics", workshop.findMechanics());
        model.addAttribute("specialties", workshop.findSpecialties());
        model.addAttribute("active", "mechanics");
        return "mechanics/list";
    }
}
