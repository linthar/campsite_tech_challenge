package com.upgrade.campsite.rest;

import com.upgrade.campsite.rest.dto.ReservationRequest;
import com.upgrade.campsite.model.Reservation;
import com.upgrade.campsite.repository.OccupiedDateRepository;
import com.upgrade.campsite.repository.ReservationRepository;
import com.upgrade.campsite.service.OccupiedDateService;
import com.upgrade.campsite.service.ReservationService;
import com.upgrade.campsite.utils.ReservationTestUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class UpdateReservationControllerTest {

    // this class is an End-to-End test case suite (from HttpClient to DB)
    // The idea is to detect any layer interoperation problem (rest/service/repository/DB)
    // and also verify that Rest API response is ok

    // code branching test will be performed in other Unit tests (mocking some layers)

    final String ENDPOINT_URL = "/reservation";
    final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();


    @Inject
    @Client("/")
    RxHttpClient client;

    @Inject
    private OccupiedDateService occupiedDateService;

    @Inject
    private ReservationService service;

    @Inject
    private OccupiedDateRepository occupiedDateRepository;

    @Inject
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        occupiedDateRepository.deleteAll();
        reservationRepository.deleteAll();
        // starts with an empty DB to avoid false negative in tests
        // (reservation dates are random)
    }

    @AfterEach
    void tearDown() {
        // cleanup the DB for just in case for Next Tests
        occupiedDateRepository.deleteAll();
        reservationRepository.deleteAll();
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