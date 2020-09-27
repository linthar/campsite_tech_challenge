package com.upgrade.campsite.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservationRequestTest {

    static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    static final LocalDate TODAY = LocalDate.now();

    @Test
    void getConstructor() {

        String fullname = "fullname_" + RANDOM.nextInt(1000);
        String email = "Email_" + RANDOM.nextInt(1000) + "@a.com";
        LocalDate arrivalDate = TODAY.plusDays(RANDOM.nextInt(0, 10));
        LocalDate departureDate = arrivalDate.plusDays(RANDOM.nextInt(3, 15));

        ReservationRequest r = new ReservationRequest(email, fullname, arrivalDate, departureDate);

        assertEquals(email, r.getEmail());
        assertEquals(fullname, r.getFullname());
        assertEquals(arrivalDate, r.getArrivalDate());
        assertEquals(departureDate, r.getDepartureDate());

    }

    @Test
    void getGettersSetters() {
        ReservationRequest r = new ReservationRequest();

        String fullname = "fullname_" + RANDOM.nextInt(1000);
        String email = "Email_" + RANDOM.nextInt(1000) + "@a.com";
        LocalDate arrivalDate = TODAY.plusDays(RANDOM.nextInt(0, 10));
        LocalDate departureDate = arrivalDate.plusDays(RANDOM.nextInt(3, 15));

        r.setEmail(email);
        r.setFullname(fullname);
        r.setArrivalDate(arrivalDate);
        r.setDepartureDate(departureDate);

        assertEquals(email, r.getEmail());
        assertEquals(fullname, r.getFullname());
        assertEquals(arrivalDate, r.getArrivalDate());
        assertEquals(departureDate, r.getDepartureDate());

    }




}