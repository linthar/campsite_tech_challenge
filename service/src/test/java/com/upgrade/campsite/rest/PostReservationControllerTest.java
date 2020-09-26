package com.upgrade.campsite.rest;

import com.upgrade.campsite.dto.ReservationRequest;
import com.upgrade.campsite.model.OccupiedDate;
import com.upgrade.campsite.model.Reservation;
import com.upgrade.campsite.repository.OccupiedDateRepository;
import com.upgrade.campsite.repository.ReservationRepository;
import com.upgrade.campsite.service.OccupiedDateService;
import com.upgrade.campsite.service.ReservationService;
import com.upgrade.campsite.utils.ReservationUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
class PostReservationControllerTest {

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

    }

    @AfterEach
    void tearDown() {
        // cleanup the DB for Next Test
        occupiedDateRepository.deleteAll();
        reservationRepository.deleteAll();
    }

    @Test
    void create() {
        URI uri = UriBuilder.of(ENDPOINT_URL).build();
        ReservationRequest reservationRequest = ReservationUtils.getRandomReservation();
        MutableHttpRequest request = HttpRequest.POST(uri, reservationRequest);
        HttpResponse<Reservation> httpResponse = client.toBlocking().exchange(request, Reservation.class);

        assertEquals(HttpStatus.OK, httpResponse.getStatus(), "response status is wrong");
        Optional<Reservation> oBody = httpResponse.getBody();
        assertTrue(oBody.isPresent(), "body is empty");

        // check the response
        Reservation newReservation = oBody.get();

        LocalDate arrivalDate = reservationRequest.getArrivalDate();
        LocalDate departureDate = reservationRequest.getDepartureDate();


        assertNotNull(newReservation.getId(), "Id must be provided");
        assertEquals(reservationRequest.getEmail(), newReservation.getEmail(), "Email is wrong");
        assertEquals(reservationRequest.getFullname(), newReservation.getFullname(), "Fullname is wrong");
        assertEquals(arrivalDate, newReservation.getArrivalDate(), "ArrivalDate is wrong");
        assertEquals(departureDate, newReservation.getDepartureDate(), "DepartureDate is wrong");

        //Now check that the reservation is stored into the DB
        Optional<Reservation> oEntity = service.findByID(newReservation.getId());
        assertTrue(oEntity.isPresent(), "an entity must be found by ID");
        Reservation entity = oEntity.get();

        //check reservation entity data
        assertEquals(newReservation.getId(), entity.getId(),  "entity Id is wrong");
        assertEquals(reservationRequest.getEmail(), entity.getEmail(), "entity Email is wrong");
        assertEquals(reservationRequest.getFullname(), entity.getFullname(), "entity Fullname is wrong");
        assertEquals(arrivalDate, entity.getArrivalDate(), "entity ArrivalDate is wrong");
        assertEquals(departureDate, entity.getDepartureDate(), "entity DepartureDate is wrong");

        // check
        List<OccupiedDate> occupiedDates = occupiedDateService.findAllBetweenDates(arrivalDate, departureDate);

        // check each day between fromDate and toDate dates is set as Occupied
        for (LocalDate date = arrivalDate; date.isBefore(departureDate.plusDays(1)); date = date.plusDays(1)) {
            assertTrue(occupiedDateService.isOccupied(date));
        }

        // check each day between fromDate and toDate dates is stored as Occupied in the DB
        for (LocalDate date = arrivalDate; date.isBefore(departureDate.plusDays(1)); date = date.plusDays(1)) {
            assertTrue(occupiedDateRepository.existsById(date));
        }


    }

}