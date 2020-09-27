package com.upgrade.campsite.rest;

import com.upgrade.campsite.model.Reservation;
import com.upgrade.campsite.rest.dto.ReservationRequest;
import com.upgrade.campsite.service.AvailabilityService;
import com.upgrade.campsite.utils.ReservationTestUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class ConcurrentCreateReservationServiceConstraintsTest extends AbstractRestControllerTest {

    @BeforeEach
    void setUp() {
        super.setUp();
    }

    @AfterEach
    void tearDown() {
        super.tearDown();
    }


    @Test
    void createConcurrent() throws InterruptedException {
        ReservationRequest r1 = ReservationTestUtils.getRandomReservation();
        ReservationRequest r2 = ReservationTestUtils.getRandomReservation();
        ReservationRequest r3 = ReservationTestUtils.getRandomReservation();
        ReservationRequest r4 = ReservationTestUtils.getRandomReservation();
        ReservationRequest r5 = ReservationTestUtils.getRandomReservation();
        ReservationRequest r6 = ReservationTestUtils.getRandomReservation();

        //try to reserve periods concurrently that have one date in common
        // all reservations compete for one date : dateToCompite
        LocalDate dateToCompite = LocalDate.now().plusDays(10);

        r1.setArrivalDate(dateToCompite);
        r1.setDepartureDate(dateToCompite);

        r2.setArrivalDate(dateToCompite);
        r2.setDepartureDate(dateToCompite.plusDays(1));

        r3.setArrivalDate(dateToCompite);
        r3.setDepartureDate(dateToCompite.plusDays(2));

        r4.setArrivalDate(dateToCompite.minusDays(2));
        r4.setDepartureDate(dateToCompite);

        r5.setArrivalDate(dateToCompite.minusDays(1));
        r5.setDepartureDate(dateToCompite);

        r6.setArrivalDate(dateToCompite.minusDays(1));
        r6.setDepartureDate(dateToCompite.plusDays(1));


        final List<ReservationRequest> reservationList = Arrays.asList(r1, r2, r3, r4, r5, r6);
        final Map<Integer, HttpStatus> failuresMap = new HashMap();
        final Integer[] successIndexHolder = {-1};
        final Reservation[] successReservationHolder = {null};


        Map<LocalDate, String> availabilityReportBefore = availabilityService.getAvailability(dateToCompite.minusDays(3), dateToCompite.plusDays(3));

        // all dates must be free before the test
        for (String value : availabilityReportBefore.values()) {
            assertEquals(AvailabilityService.NOT_OCCUPIED_DATE, value);
        }

        // now try to run N threads (one for each reservation) and reserve concurrently all periods
        // only one must success
        int numberOfThreads = reservationList.size();
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            int iCopy = i;
            executorService.execute(() -> {
                try {
                    // only one will success
                    successReservationHolder[0] = createReservationRequest(iCopy, reservationList);
                    successIndexHolder[0] = iCopy;

                } catch (HttpClientResponseException e) {
                    failuresMap.put(iCopy, e.getStatus());
                }
                latch.countDown();
            });
        }

        latch.await();

        assertEquals(numberOfThreads - 1, failuresMap.size(), "threads that must fail (only one should success)");

        // success registration check
        ReservationRequest succeededReservation = reservationList.get(successIndexHolder[0]);
        assertNotNull(succeededReservation);
        Map<LocalDate, String> availabilityReport = availabilityService.getAvailability(dateToCompite.minusDays(3), dateToCompite.plusDays(3));


        // all dates must be free except those from succeeded Reservation
        List<LocalDate> succeededDates = Stream.iterate(succeededReservation.getArrivalDate(), date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(succeededReservation.getArrivalDate(), succeededReservation.getDepartureDate()) + 1)
                .collect(Collectors.toList());


        for (Map.Entry<LocalDate, String> entry : availabilityReport.entrySet()) {
            if (succeededDates.contains(entry.getKey())) {
                assertEquals(AvailabilityService.OCCUPIED_DATE, entry.getValue(), "availability check error for date " + entry.getKey());
            } else {
                assertEquals(AvailabilityService.NOT_OCCUPIED_DATE, entry.getValue(), "availability check error for date " + entry.getKey());
            }
        }
    }

    final String ENDPOINT_URL = "/reservation";

    Reservation createReservationRequest(int index, List<ReservationRequest> reservationList) {
        ReservationRequest r = reservationList.get(index);

        // uri = http://[HOST:PORT]]/reservation
        MutableHttpRequest request = HttpRequest.POST(ENDPOINT_URL, r);
        HttpResponse<Reservation> httpResponse = client.toBlocking().exchange(request, Reservation.class);

        assertEquals(HttpStatus.OK, httpResponse.getStatus(), "response status is wrong");
        Optional<Reservation> oBody = httpResponse.getBody();
        assertTrue(oBody.isPresent(), "body is empty");

        // check the response
        Reservation newReservation = oBody.get();
        return oBody.get();

    }


}