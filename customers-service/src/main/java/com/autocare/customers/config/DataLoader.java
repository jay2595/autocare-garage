package com.autocare.customers.config;

import com.autocare.customers.model.Customer;
import com.autocare.customers.model.Vehicle;
import com.autocare.customers.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Seeds demo data on startup so the app is never an empty shell.
 */
@Component
@Profile("!test")
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final CustomerRepository customers;

    public DataLoader(CustomerRepository customers) {
        this.customers = customers;
    }

    @Override
    public void run(String... args) {
        if (customers.count() > 0) {
            return;
        }

        customers.save(customer("Priya", "Sharma", "+91 98200 11223", "priya.sharma@example.com",
                "18 Marine Drive", "Mumbai",
                vehicle("Honda", "City", 2019, "MH01AB1234", "MAKGM763XKN123456", 64200),
                vehicle("Maruti", "Swift", 2015, "MH01CD5678", "MA3EJKD1S00234567", 118400)));

        customers.save(customer("Daniel", "Okafor", "+44 7700 900412", "d.okafor@example.com",
                "42 Bramley Road", "Leeds",
                vehicle("Volkswagen", "Golf", 2021, "LD21XKR", "WVWZZZAUZMP098765", 28900)));

        customers.save(customer("Mei", "Tanaka", "+81 90 1234 5678", "mei.tanaka@example.com",
                "3-14 Sakura Street", "Osaka",
                vehicle("Toyota", "Corolla", 2018, "OSK4821", "JTDBR32E830123456", 87650),
                vehicle("Subaru", "Forester", 2022, "OSK9033", "JF2SKAUC5NH456789", 19300)));

        customers.save(customer("Carlos", "Mendes", "+55 11 96543 2100", "carlos.mendes@example.com",
                "Rua Augusta 1200", "Sao Paulo",
                vehicle("Fiat", "Argo", 2020, "SPA7C21", "9BD358A4XLYE12345", 52100)));

        customers.save(customer("Aisha", "Bello", "+234 803 555 0199", "aisha.bello@example.com",
                "7 Adeola Odeku", "Lagos",
                vehicle("Ford", "Ranger", 2017, "LAG882KJ", "MNBLMFE50HW123456", 143700)));

        customers.save(customer("Jonas", "Lindqvist", "+46 70 123 45 67", "jonas.l@example.com",
                "Vasagatan 22", "Stockholm",
                vehicle("Volvo", "XC60", 2023, "STO441X", "YV1UZK8VDP1234567", 9800)));

        log.info("Seeded {} demo customers", customers.count());
    }

    private Customer customer(String first, String last, String phone, String email,
                              String address, String city, Vehicle... vehicles) {
        Customer c = new Customer();
        c.setFirstName(first);
        c.setLastName(last);
        c.setPhone(phone);
        c.setEmail(email);
        c.setAddress(address);
        c.setCity(city);
        for (Vehicle v : vehicles) {
            c.addVehicle(v);
        }
        return c;
    }

    private Vehicle vehicle(String make, String model, int year, String reg, String vin, int mileage) {
        Vehicle v = new Vehicle();
        v.setMake(make);
        v.setModel(model);
        v.setYear(year);
        v.setRegistrationNumber(reg);
        v.setVin(vin);
        v.setMileage(mileage);
        return v;
    }
}
