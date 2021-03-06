package com.upgrade.campsite.rest;

import com.upgrade.campsite.model.Reservation;
import com.upgrade.campsite.rest.dto.ReservationRequest;
import com.upgrade.campsite.utils.ReservationTestUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class DeleteReservationControllerTest extends AbstractRestControllerTest {

    // this class is an End-to-End test case suite (from HttpClient to DB)
    // The idea is to detect any layer interoperation problem (rest/service/repository/DB)
    // and also verify that Rest API response is ok

    // code branching test will be performed in other Unit tests (mocking some layers)

    final String ENDPOINT_URL = "/reservation";
    final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    @BeforeEach
    void setUp() {
        super.setUp();
    }

    @AfterEach
    void tearDown() {
        super.tearDown();
    }

   @Test
    void delete() {
        ReservationRequest reservationRequest = ReservationTestUtils.getRandomReservation();
        Reservation entity = service.create(reservationRequest.getEmail(), reservationRequest.getFullname(),
                reservationRequest.getArrivalDate(), reservationRequest.getDepartureDate());

        // asserts entity was stored in DB
        assertTrue(service.findByID(entity.getId()).isPresent());

        // occupied dates are in the DB too
        for (LocalDate date = reservationRequest.getArrivalDate(); date.isBefore(reservationRequest.getDepartureDate().plusDays(1)); date = date.plusDays(1)) {
            assertTrue(occupiedDateService.isOccupied(date));
        }

        URI uri = UriBuilder.of(ENDPOINT_URL + "/" + entity.getId()).build();
        MutableHttpRequest request = HttpRequest.DELETE(uri, null);
        HttpResponse httpResponse = client.toBlocking().exchange(request);

        assertEquals(HttpStatus.NO_CONTENT, httpResponse.getStatus(), "response status is wrong");

        // asserts entity was deleted from DB
        assertFalse(service.findByID(entity.getId()).isPresent());
        // occupied dates are deleted from DB too
        for (LocalDate date = reservationRequest.getArrivalDate(); date.isBefore(reservationRequest.getDepartureDate().plusDays(1)); date = date.plusDays(1)) {
            assertFalse(occupiedDateService.isOccupied(date));
        }

    }


    @Test
    void deleteIsIdempotent() {

        UUID inexistentId = UUID.randomUUID();
        // asserts entity does not exists in the DB
        assertFalse(service.findByID(inexistentId).isPresent());

        URI uri = UriBuilder.of(ENDPOINT_URL + "/" + inexistentId).build();
        MutableHttpRequest request = HttpRequest.DELETE(uri, null);
        HttpResponse httpResponse = client.toBlocking().exchange(request);

        assertEquals(HttpStatus.NO_CONTENT, httpResponse.getStatus(), "response status is wrong");

        // asserts entity does not exists in the DB
        assertFalse(service.findByID(inexistentId).isPresent());

    }


}