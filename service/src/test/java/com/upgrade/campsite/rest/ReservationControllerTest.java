package com.upgrade.campsite.rest;

import com.upgrade.campsite.dto.CreateReservation;
import com.upgrade.campsite.dto.DateAvailavility;
import com.upgrade.campsite.model.Reservation;
import io.micronaut.core.type.Argument;
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
import java.util.List;
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

    private CreateReservation getCreateReservation() {
        CreateReservation createReservation = new CreateReservation();
        createReservation.setFullname("fullname_"+RANDOM.nextInt(1000));
        createReservation.setEmail("Email_"+RANDOM.nextInt(1000));
        LocalDate today = LocalDate.now();
        createReservation.setArrivalDate(today.plusDays(1));
        createReservation.setDepartureDate(today.plusDays(2));
        return createReservation;
    }

    @Test
    void create() {
        URI uri = UriBuilder.of(ENDPOINT_URL).build();
        CreateReservation createReservation = getCreateReservation();
        MutableHttpRequest request = HttpRequest.POST(uri, createReservation);
        HttpResponse<Reservation> httpResponse = client.toBlocking().exchange(request, Reservation.class);

        assertEquals(HttpStatus.OK, httpResponse.getStatus(), "response status is wrong");
        Optional<Reservation> oBody = httpResponse.getBody();
        assertTrue(oBody.isPresent(), "body is empty");

        // check the response
        Reservation newReservation = oBody.get();

        assertNotNull(newReservation.getId(), "Id must be provided");
        assertEquals(createReservation.getEmail(), newReservation.getEmail(), "Email is wrong");
        assertEquals(createReservation.getFullname(), newReservation.getFullname(), "Fullname is wrong");
        assertEquals(createReservation.getArrivalDate(), newReservation.getArrivalDate(), "ArrivalDate is wrong");
        assertEquals(createReservation.getDepartureDate(), newReservation.getDepartureDate(), "DepartureDate is wrong");


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
        CreateReservation updateReservation = getCreateReservation();

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