package com.upgrade.campsite.utils;

import com.upgrade.campsite.dto.ReservationRequest;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

public class ReservationTestUtils {

    static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    public static ReservationRequest getRandomReservation() {
        ReservationRequest reservationRequest = new ReservationRequest();
        reservationRequest.setFullname("fullname_" + RANDOM.nextInt(1000));
        reservationRequest.setEmail("Email_" + RANDOM.nextInt(1000) + "@a.com");
        LocalDate today = LocalDate.now();

        int stayDays = RANDOM.nextInt(0,2);
        int daysAhead = RANDOM.nextInt(1,25);
        LocalDate arrival = today.plusDays(daysAhead);
        reservationRequest.setArrivalDate(arrival);
        reservationRequest.setDepartureDate(arrival.plusDays(stayDays));
        return reservationRequest;
    }

}
