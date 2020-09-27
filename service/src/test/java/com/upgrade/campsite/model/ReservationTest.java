package com.upgrade.campsite.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservationTest {

    static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    static final LocalDate TODAY = LocalDate.now();


    @Test
    void getConstructor() {

        UUID id = UUID.randomUUID();
        String fullname = "fullname_" + RANDOM.nextInt(1000);
        String email = "Email_" + RANDOM.nextInt(1000) + "@a.com";
        LocalDate arrivalDate = TODAY.plusDays(RANDOM.nextInt(0, 10));
        LocalDate departureDate = arrivalDate.plusDays(RANDOM.nextInt(3, 15));

        Reservation r = new Reservation(id, email, fullname, arrivalDate, departureDate);

        assertEquals(id, r.getId());
        assertEquals(email, r.getEmail());
        assertEquals(fullname, r.getFullname());
        assertEquals(arrivalDate, r.getArrivalDate());
        assertEquals(departureDate, r.getDepartureDate());

    }

    @Test
    void getGettersSetters() {
        Reservation r = new Reservation();

        UUID id = UUID.randomUUID();
        String fullname = "fullname_" + RANDOM.nextInt(1000);
        String email = "Email_" + RANDOM.nextInt(1000) + "@a.com";
        LocalDate arrivalDate = TODAY.plusDays(RANDOM.nextInt(0, 10));
        LocalDate departureDate = arrivalDate.plusDays(RANDOM.nextInt(3, 15));

        r.setId(id);
        r.setEmail(email);
        r.setFullname(fullname);
        r.setArrivalDate(arrivalDate);
        r.setDepartureDate(departureDate);

        assertEquals(id, r.getId());
        assertEquals(email, r.getEmail());
        assertEquals(fullname, r.getFullname());
        assertEquals(arrivalDate, r.getArrivalDate());
        assertEquals(departureDate, r.getDepartureDate());

    }

}