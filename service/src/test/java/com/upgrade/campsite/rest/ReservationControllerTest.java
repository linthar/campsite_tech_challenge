package com.upgrade.campsite.rest;

import com.upgrade.campsite.dto.ReservationRequest;
import com.upgrade.campsite.model.Reservation;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.net.URI;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class ReservationControllerTest {


    final String ENDPOINT_URL = "/reservation";
    final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();


    @Inject
    @Client("/")
    RxHttpClient client;

    private ReservationRequest getCreateReservation() {
        ReservationRequest reservationRequest = new ReservationRequest();
        reservationRequest.setFullname("fullname_" + RANDOM.nextInt(1000));
        reservationRequest.setEmail("Email_" + RANDOM.nextInt(1000) + "@a.com");
        LocalDate today = LocalDate.now();
        reservationRequest.setArrivalDate(today.plusDays(1));
        reservationRequest.setDepartureDate(today.plusDays(2));
        return reservationRequest;
    }

    @Test
    void create() {
        URI uri = UriBuilder.of(ENDPOINT_URL).build();
        ReservationRequest reservationRequest = getCreateReservation();
        MutableHttpRequest request = HttpRequest.POST(uri, reservationRequest);
        HttpResponse<Reservation> httpResponse = client.toBlocking().exchange(request, Reservation.class);

        assertEquals(HttpStatus.OK, httpResponse.getStatus(), "response status is wrong");
        Optional<Reservation> oBody = httpResponse.getBody();
        assertTrue(oBody.isPresent(), "body is empty");

        // check the response
        Reservation newReservation = oBody.get();

        assertNotNull(newReservation.getId(), "Id must be provided");
        assertEquals(reservationRequest.getEmail(), newReservation.getEmail(), "Email is wrong");
        assertEquals(reservationRequest.getFullname(), newReservation.getFullname(), "Fullname is wrong");
        assertEquals(reservationRequest.getArrivalDate(), newReservation.getArrivalDate(), "ArrivalDate is wrong");
        assertEquals(reservationRequest.getDepartureDate(), newReservation.getDepartureDate(), "DepartureDate is wrong");


    }

    @Test
    void get() {
        UUID id = UUID.randomUUID();
        String email = "aa@bb.com";
        String fullname = "John Smith";
        LocalDate today = LocalDate.now();
        LocalDate arrivalDate = today.plusDays(1);
        LocalDate departureDate = today.plusDays(2);


        URI uri = UriBuilder.of(ENDPOINT_URL + "/" + id).build();
        MutableHttpRequest request = HttpRequest.GET(uri);
        HttpResponse<Reservation> httpResponse = client.toBlocking().exchange(request, Reservation.class);

        assertEquals(HttpStatus.OK, httpResponse.getStatus(), "response status is wrong");
        Optional<Reservation> oBody = httpResponse.getBody();
        assertTrue(oBody.isPresent(), "body is empty");

        // check the response
        Reservation newReservation = oBody.get();

        assertEquals(id, newReservation.getId(), "Id is wrong");
        assertEquals(email, newReservation.getEmail(), "Email is wrong");
        assertEquals(fullname, newReservation.getFullname(), "Fullname is wrong");
        assertEquals(arrivalDate, newReservation.getArrivalDate(), "ArrivalDate is wrong");
        assertEquals(departureDate, newReservation.getDepartureDate(), "DepartureDate is wrong");
    }

    @Test
    void update() {

        UUID id = UUID.randomUUID();
        ReservationRequest updateReservation = getCreateReservation();

        URI uri = UriBuilder.of(ENDPOINT_URL + "/" + id).build();
        MutableHttpRequest request = HttpRequest.PATCH(uri, updateReservation);
        HttpResponse<Reservation> httpResponse = client.toBlocking().exchange(request, Reservation.class);

        assertEquals(HttpStatus.OK, httpResponse.getStatus(), "response status is wrong");
        Optional<Reservation> oBody = httpResponse.getBody();
        assertTrue(oBody.isPresent(), "body is empty");

        // check the response
        Reservation newReservation = oBody.get();

        assertEquals(id, newReservation.getId(), "Id is wrong");
        assertEquals(updateReservation.getEmail(), newReservation.getEmail(), "Email is wrong");
        assertEquals(updateReservation.getFullname(), newReservation.getFullname(), "Fullname is wrong");
        assertEquals(updateReservation.getArrivalDate(), newReservation.getArrivalDate(), "ArrivalDate is wrong");
        assertEquals(updateReservation.getDepartureDate(), newReservation.getDepartureDate(), "DepartureDate is wrong");
    }

    @Test
    void delete() {
        UUID id = UUID.randomUUID();
        URI uri = UriBuilder.of(ENDPOINT_URL + "/" + id).build();
        MutableHttpRequest request = HttpRequest.DELETE(uri, null);
        HttpResponse httpResponse = client.toBlocking().exchange(request);

        assertEquals(HttpStatus.NO_CONTENT, httpResponse.getStatus(), "response status is wrong");
    }
}