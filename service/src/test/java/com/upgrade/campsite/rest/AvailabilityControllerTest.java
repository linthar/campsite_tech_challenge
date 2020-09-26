package com.upgrade.campsite.rest;

import com.upgrade.campsite.service.OccupiedDateService;
import com.upgrade.campsite.utils.AvailavilityConstants;
import io.micronaut.core.type.Argument;
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
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class AvailabilityControllerTest {

    // this class is an End-to-End test case suite (from HttpClient to DB)
    // The idea is to detect any layer interoperation problem (rest/service/repository/DB)
    // and also verify that Rest API response is ok

    // code branching test will be performed in other Unit tests (mocking some layers)

    final String ENDPOINT_URL = "/availability";

    @Inject
    @Client("/")
    RxHttpClient client;

    @Inject
    private OccupiedDateService occupiedDateService;

    final LocalDate TODAY = LocalDate.now();
    private Set<LocalDate> TAKEN_DATES;
    final UUID RESERVATION_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        // TAKEN_DATES is a set to aviod taking care of repeated dates
        TAKEN_DATES = new HashSet<LocalDate>();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        LocalDate randomDate;
        for (int i = 0; i < 10; i++) {
            int rPlus = random.nextInt(0, 31);
            randomDate = TODAY.plusDays(rPlus);

            if (randomDate.isAfter(TODAY.plusMonths(1))) {
                // to avoid taking care of 28/30/31 days months
                // depends on when the test is executed
                randomDate = TODAY.plusMonths(1);
            }
            TAKEN_DATES.add(randomDate);
        }

        // set the random dates as occupied in the DB
        occupiedDateService.saveAll(RESERVATION_ID, new ArrayList<LocalDate>(TAKEN_DATES));
    }

    @AfterEach
    void tearDown() {
        // cleanup the occupiedDate table for next test
        occupiedDateService.deleteAllForReservationID(RESERVATION_ID);
    }


    @Test
    public void testFromDateToDateResponse() throws Exception {
        // This test specifies fromDate and toDate parameters with a given dates
        // e.g: http://localhost:8080/availability?fromDate=2020-11-01&toDate=2020-11-22

        LocalDate fromDate = TODAY.plusDays(1);
        LocalDate toDate = TODAY.plusDays(1);

        URI uri = UriBuilder.of(ENDPOINT_URL + "?fromDate=" + fromDate + "&toDate=" + toDate).build();
        MutableHttpRequest request = HttpRequest.GET(uri);
        HttpResponse<Map<LocalDate, String>> httpResponse = client.toBlocking().exchange(request, Argument.of(Map.class, LocalDate.class, String.class));

        assertEquals(HttpStatus.OK, httpResponse.getStatus(), "response status is wrong");
        Optional<Map<LocalDate, String>> oBody = httpResponse.getBody();
        assertTrue(oBody.isPresent(), "body is empty");


        // check the availability response for the given dates range
        assert_availavility_for_dates_range(oBody.get(), fromDate, toDate);
    }


    @Test
    public void testFromDateResponse() throws Exception {
        // This test specifies fromDate and toDate parameters with a given dates
        // e.g: http://localhost:8080/availability?fromDate=2020-11-01

        LocalDate fromDate = TODAY.plusDays(5);

        URI uri = UriBuilder.of(ENDPOINT_URL + "?fromDate=" + fromDate).build();
        MutableHttpRequest request = HttpRequest.GET(uri);
        HttpResponse<Map<LocalDate, String>> httpResponse = client.toBlocking().exchange(request, Argument.of(Map.class, LocalDate.class, String.class));

        assertEquals(HttpStatus.OK, httpResponse.getStatus(), "response status is wrong");
        Optional<Map<LocalDate, String>> oBody = httpResponse.getBody();
        assertTrue(oBody.isPresent(), "body is empty");

        //Endpoint Default values are
        LocalDate defaultToDate = TODAY.plusMonths(1);


        // check the availability response for the given dates range
        assert_availavility_for_dates_range(oBody.get(), fromDate, defaultToDate);
    }


    @Test
    public void testToDateResponse() throws Exception {
        // This test specifies fromDate and toDate parameters with a given dates
        // e.g: http://localhost:8080/availability?toDate=2020-11-22

        LocalDate toDate = TODAY.plusDays(22);

        URI uri = UriBuilder.of(ENDPOINT_URL + "?toDate=" + toDate).build();
        MutableHttpRequest request = HttpRequest.GET(uri);
        HttpResponse<Map<LocalDate, String>> httpResponse = client.toBlocking().exchange(request, Argument.of(Map.class, LocalDate.class, String.class));

        assertEquals(HttpStatus.OK, httpResponse.getStatus(), "response status is wrong");
        Optional<Map<LocalDate, String>> oBody = httpResponse.getBody();
        assertTrue(oBody.isPresent(), "body is empty");

        //Endpoint Default values are
        LocalDate defaultFromDate = TODAY.plusDays(1);

        // check the availability response for the given dates range
        assert_availavility_for_dates_range(oBody.get(), defaultFromDate, toDate);
    }


    @Test
    public void testDefaultDatesRangeResponse() throws Exception {
        // This test does not specifies fromDate neither toDate parameters
        // e.g: http://localhost:8080/availability

        URI uri = UriBuilder.of(ENDPOINT_URL).build();
        MutableHttpRequest request = HttpRequest.GET(uri);
        HttpResponse<Map<LocalDate, String>> httpResponse = client.toBlocking().exchange(request, Argument.of(Map.class, LocalDate.class, String.class));

        assertEquals(HttpStatus.OK, httpResponse.getStatus(), "response status is wrong");
        Optional<Map<LocalDate, String>> oBody = httpResponse.getBody();
        assertTrue(oBody.isPresent(), "body is empty");

        //Endpoint Default values are
        LocalDate defaultFromDate = TODAY.plusDays(1);
        LocalDate defaultToDate = TODAY.plusMonths(1);

        // check the availability response for the given dates range
        assert_availavility_for_dates_range(oBody.get(), defaultFromDate, defaultToDate);
    }


    private void assert_availavility_for_dates_range(Map<LocalDate, String> responseMap, LocalDate fromDate, LocalDate toDate) {

        // must return noOfDaysBetween dates
        long expectedSize = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        assertEquals(expectedSize, responseMap.size(), "response size is wrong for dates from: " +  fromDate + " to: " + toDate);

        // check each day between fromDate and toDate dates is present in the response
        for (LocalDate date = fromDate; date.isBefore(toDate); date = date.plusDays(1)) {
            String dateAvailavilty = responseMap.get(date);
            // checking each date exists in the map
            assertNotNull(dateAvailavilty, date + " availavilty is missing");

            // checking each date availability value
            if (TAKEN_DATES.contains(date)) {
                assertEquals(AvailavilityConstants.OCCUPIED_DATE, dateAvailavilty, date + " availavilty value is wrong");
            } else {
                assertEquals(AvailavilityConstants.NOT_OCCUPIED_DATE, dateAvailavilty, date + " availavilty value is wrong");
            }
        }
    }


}