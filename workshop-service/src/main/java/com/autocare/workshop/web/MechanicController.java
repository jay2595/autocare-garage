package com.autocare.workshop.web;

import com.autocare.workshop.model.Mechanic;
import com.autocare.workshop.model.Specialty;
import com.autocare.workshop.repository.MechanicRepository;
import com.autocare.workshop.repository.SpecialtyRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workshop")
public class MechanicController {

    private final MechanicRepository mechanics;
    private final SpecialtyRepository specialties;

    public MechanicController(MechanicRepository mechanics, SpecialtyRepository specialties) {
        this.mechanics = mechanics;
        this.specialties = specialties;
    }

    @GetMapping("/mechanics")
    public List<Mechanic> listMechanics() {
        return mechanics.findAll();
    }

    @GetMapping("/mechanics/{id}")
    public Mechanic getMechanic(@PathVariable Long id) {
        return mechanics.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic", id));
    }

    @PostMapping("/mechanics")
    @ResponseStatus(HttpStatus.CREATED)
    public Mechanic createMechanic(@Valid @RequestBody Mechanic mechanic) {
        mechanic.setId(null);
        return mechanics.save(mechanic);
    }

    @GetMapping("/specialties")
    public List<Specialty> listSpecialties() {
        return specialties.findAll();
    }
}
