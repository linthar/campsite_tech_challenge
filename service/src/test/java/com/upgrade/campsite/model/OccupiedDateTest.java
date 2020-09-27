package com.upgrade.campsite.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OccupiedDateTest {

    static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    static final LocalDate TODAY = LocalDate.now();

    @Test
    void getConstructor() {
        UUID reservationId = UUID.randomUUID();
        LocalDate date = TODAY.plusDays(RANDOM.nextInt(0, 10));

        OccupiedDate od = new OccupiedDate(date, reservationId);

        assertEquals(date, od.getDate());
        assertEquals(reservationId, od.getReservationId());

    }

    @Test
    void getGettersSetters() {
        OccupiedDate od = new OccupiedDate();

        UUID reservationId = UUID.randomUUID();
        LocalDate date = TODAY.plusDays(RANDOM.nextInt(0, 10));

        od.setDate(date);
        od.setReservationId(reservationId);

        assertEquals(date, od.getDate());
        assertEquals(reservationId, od.getReservationId());
    }

}