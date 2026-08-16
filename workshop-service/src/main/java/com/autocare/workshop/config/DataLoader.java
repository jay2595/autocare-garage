package com.autocare.workshop.config;

import com.autocare.workshop.model.JobStatus;
import com.autocare.workshop.model.Mechanic;
import com.autocare.workshop.model.ServiceJob;
import com.autocare.workshop.model.Specialty;
import com.autocare.workshop.repository.MechanicRepository;
import com.autocare.workshop.repository.ServiceJobRepository;
import com.autocare.workshop.repository.SpecialtyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Profile("!test")
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final MechanicRepository mechanics;
    private final SpecialtyRepository specialties;
    private final ServiceJobRepository jobs;

    public DataLoader(MechanicRepository mechanics, SpecialtyRepository specialties,
                      ServiceJobRepository jobs) {
        this.mechanics = mechanics;
        this.specialties = specialties;
        this.jobs = jobs;
    }

    @Override
    public void run(String... args) {
        if (mechanics.count() > 0) {
            return;
        }

        Specialty engine = specialties.save(new Specialty("Engine"));
        Specialty electrical = specialties.save(new Specialty("Electrical"));
        Specialty bodywork = specialties.save(new Specialty("Bodywork"));
        Specialty diagnostics = specialties.save(new Specialty("Diagnostics"));
        Specialty tyres = specialties.save(new Specialty("Tyres & Brakes"));

        Mechanic amos = mechanic("Amos", "Whitfield", LocalDate.of(2016, 3, 14), engine, diagnostics);
        Mechanic lena = mechanic("Lena", "Novak", LocalDate.of(2019, 8, 1), electrical, diagnostics);
        Mechanic tariq = mechanic("Tariq", "Hassan", LocalDate.of(2021, 1, 18), bodywork);
        Mechanic sofia = mechanic("Sofia", "Reyes", LocalDate.of(2022, 6, 6), tyres, engine);

        job(1L, amos.getId(), "Oil change and full inspection at 60k service interval",
                JobStatus.COMPLETED, "89.00", 6);
        job(1L, lena.getId(), "Intermittent dashboard warning light - diagnose ECU fault",
                JobStatus.DIAGNOSING, "140.00", 1);
        job(2L, sofia.getId(), "Front brake pads and discs replacement",
                JobStatus.IN_PROGRESS, "310.50", 2);
        job(3L, tariq.getId(), "Rear bumper respray after car park scrape",
                JobStatus.RECEIVED, "480.00", 0);
        job(4L, amos.getId(), "Timing belt and water pump replacement",
                JobStatus.IN_PROGRESS, "725.00", 3);
        job(5L, lena.getId(), "Battery replacement and alternator check",
                JobStatus.DELIVERED, "215.00", 11);
        job(6L, sofia.getId(), "Four new tyres, alignment and balancing",
                JobStatus.COMPLETED, "640.00", 4);
        job(7L, amos.getId(), "Clutch judder on pull away - investigate",
                JobStatus.RECEIVED, null, 0);
        job(8L, tariq.getId(), "Driver door dent removal and panel refinish",
                JobStatus.IN_PROGRESS, "395.00", 2);
        job(5L, lena.getId(), "Reverse camera not powering on",
                JobStatus.DIAGNOSING, "95.00", 1);

        log.info("Seeded {} mechanics and {} service jobs", mechanics.count(), jobs.count());
    }

    private Mechanic mechanic(String first, String last, LocalDate hired, Specialty... skills) {
        Mechanic m = new Mechanic();
        m.setFirstName(first);
        m.setLastName(last);
        m.setHireDate(hired);
        for (Specialty s : skills) {
            m.addSpecialty(s);
        }
        return mechanics.save(m);
    }

    private void job(Long vehicleId, Long mechanicId, String description,
                     JobStatus status, String cost, int daysAgo) {
        ServiceJob j = new ServiceJob();
        j.setVehicleId(vehicleId);
        j.setMechanicId(mechanicId);
        j.setDescription(description);
        j.setStatus(status);
        j.setEstimatedCost(cost == null ? null : new BigDecimal(cost));
        j.setCreatedAt(LocalDateTime.now().minusDays(daysAgo));
        if (!status.isOpen()) {
            j.setCompletedAt(LocalDateTime.now().minusDays(Math.max(0, daysAgo - 1)));
        }
        jobs.save(j);
    }
}
