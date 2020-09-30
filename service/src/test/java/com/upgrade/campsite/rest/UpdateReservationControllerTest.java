package com.upgrade.campsite.rest;

import com.upgrade.campsite.model.Reservation;
import com.upgrade.campsite.rest.dto.ReservationRequest;
import com.upgrade.campsite.utils.ReservationTestUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class UpdateReservationControllerTest  extends  AbstractRestControllerTest  {

    // this class is an End-to-End test case suite (from HttpClient to DB)
    // The idea is to detect any layer interoperation problem (rest/service/repository/DB)
    // and also verify that Rest API response is ok

    // code branching test will be performed in other Unit tests (mocking some layers)

    final String ENDPOINT_URL = "/reservation";
    final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    final LocalDate TODAY = LocalDate.now();


    @BeforeEach
    void setUp() {
        super.setUp();
    }

    @AfterEach
    void tearDown() {
        super.tearDown();
    }

    @Test
    void update() {

        ReservationRequest reservationRequest = ReservationTestUtils.getRandomReservation();
        Reservation entity = service.create(reservationRequest.getEmail(), reservationRequest.getFullname(),
                reservationRequest.getArrivalDate(), reservationRequest.getDepartureDate());

        // asserts entity was stored in DB
        Optional<Reservation> inDB = service.findByID(entity.getId());
        assertTrue(inDB.isPresent());
        assertEquals(entity.getId(), inDB.get().getId());


        ReservationRequest updateReservation = ReservationTestUtils.getRandomReservation();

        URI uri = UriBuilder.of(ENDPOINT_URL + "/" + entity.getId()).build();
        MutableHttpRequest request = HttpRequest.PATCH(uri, updateReservation);
        HttpResponse<Reservation> httpResponse = client.toBlocking().exchange(request, Reservation.class);

        assertEquals(HttpStatus.OK, httpResponse.getStatus(), "response status is wrong");
        Optional<Reservation> oBody = httpResponse.getBody();
        assertTrue(oBody.isPresent(), "body is empty");

        // check the response
        Reservation newReservation = oBody.get();

        assertEquals(entity.getId(), newReservation.getId(), "Id is wrong");
        assertEquals(updateReservation.getEmail(), newReservation.getEmail(), "Email is wrong");
        assertEquals(updateReservation.getFullname(), newReservation.getFullname(), "Fullname is wrong");
        assertEquals(updateReservation.getArrivalDate(), newReservation.getArrivalDate(), "ArrivalDate is wrong");
        assertEquals(updateReservation.getDepartureDate(), newReservation.getDepartureDate(), "DepartureDate is wrong");
    }

    @Test
    void updateMoveOneDayAThreeDaysReservation() {
        // Try to move one day forward a Three days reservation (when the new date take is available)

        //   e.g.:      Reservation dates =  [1,2,3]
        //   we want to update the Reservation for dates  =  [2,3,4]
        //  date 4 is free, but dates 2 and 3 are taken for the same reservation
        //  the Update operation must success because dates 2 and 3 where taken for the same campers

        ReservationRequest reservationRequest = ReservationTestUtils.getRandomReservation();
        reservationRequest.setArrivalDate(TODAY.plusDays(1));
        reservationRequest.setDepartureDate(TODAY.plusDays(3));

        Reservation entity = service.create(reservationRequest.getEmail(), reservationRequest.getFullname(),
                reservationRequest.getArrivalDate(), reservationRequest.getDepartureDate());


        // asserts entity was stored in DB
        Optional<Reservation> inDB = service.findByID(entity.getId());
        assertTrue(inDB.isPresent());
        assertEquals(entity.getId(), inDB.get().getId());

        //  Now we want to move the reservation 1 day forward
        reservationRequest.setArrivalDate(TODAY.plusDays(2));
        reservationRequest.setDepartureDate(TODAY.plusDays(4));

        URI uri = UriBuilder.of(ENDPOINT_URL + "/" + entity.getId()).build();
        MutableHttpRequest request = HttpRequest.PATCH(uri, reservationRequest);
        HttpResponse<Reservation> httpResponse = client.toBlocking().exchange(request, Reservation.class);

        assertEquals(HttpStatus.OK, httpResponse.getStatus(), "response status is wrong");
        Optional<Reservation> oBody = httpResponse.getBody();
        assertTrue(oBody.isPresent(), "body is empty");

        // check the response
        Reservation newReservation = oBody.get();

        assertEquals(entity.getId(), newReservation.getId(), "Id is wrong");
        assertEquals(reservationRequest.getEmail(), newReservation.getEmail(), "Email is wrong");
        assertEquals(reservationRequest.getFullname(), newReservation.getFullname(), "Fullname is wrong");
        assertEquals(reservationRequest.getArrivalDate(), newReservation.getArrivalDate(), "ArrivalDate is wrong");
        assertEquals(reservationRequest.getDepartureDate(), newReservation.getDepartureDate(), "DepartureDate is wrong");
    }




    @Test
    void update404() {
        ReservationRequest reservationRequest = ReservationTestUtils.getRandomReservation();
        Reservation entity = service.create(reservationRequest.getEmail(), reservationRequest.getFullname(),
                reservationRequest.getArrivalDate(), reservationRequest.getDepartureDate());

        try {
            URI uri = UriBuilder.of(ENDPOINT_URL + "/" + UUID.randomUUID()).build();
            MutableHttpRequest request = HttpRequest.PATCH(uri, reservationRequest);
            HttpResponse<Reservation> httpResponse = client.toBlocking().exchange(request, Reservation.class);
            fail("HttpStatus should be 404");
        } catch (HttpClientResponseException e) {
            assertEquals(HttpStatus.NOT_FOUND, e.getStatus(), "response status is wrong");
        }
    }

}