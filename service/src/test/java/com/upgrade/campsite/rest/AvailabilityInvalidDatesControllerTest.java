package com.upgrade.campsite.rest;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.net.URI;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class AvailabilityInvalidDatesControllerTest {

    // this class is an End-to-End test case suite (HttpClient to REST service)
    // The idea is to check the error response (HTTP status & message)

    // code branching test will be performed in other Unit tests (mocking some layers)

    final String ENDPOINT_URL = "/availability";

    @Inject
    @Client("/")
    RxHttpClient client;

    final LocalDate TODAY = LocalDate.now();


    @Test
    public void testInvalidFromDate() throws Exception {
        // fromDate is wrong: must be higher or equals to Tomorrow
        // 2 months ago

        LocalDate fromDate = TODAY.minusMonths(2);
        LocalDate toDate = TODAY.plusDays(1);
        String expectedErrorMessage = "Invalid parameter fromDate '" + fromDate + "'. Must be at least tomorrow or ahead";
        inner_test_request_dates_error(fromDate, toDate, expectedErrorMessage);
    }

    @Test
    public void testInvalidFromDateBorder() throws Exception {
        // fromDate is wrong: must be higher or equals to Tomorrow
        // today
        LocalDate fromDate = TODAY;
        LocalDate toDate = TODAY.plusDays(1);
        String expectedErrorMessage = "Invalid parameter fromDate '" + fromDate + "'. Must be at least tomorrow or ahead";
        inner_test_request_dates_error(fromDate, toDate, expectedErrorMessage);
    }


    @Test
    public void testInvalidToDate() throws Exception {
        // toDate is wrong: must be lower or equals to (today + 1 month)
        // 5 months ahead

        LocalDate fromDate = TODAY.plusDays(1);
        LocalDate toDate = TODAY.plusMonths(5);
        String expectedErrorMessage = "Invalid parameter toDate '" + toDate + "'. Must be at most one moth ahead from now";
        inner_test_request_dates_error(fromDate, toDate, expectedErrorMessage);
    }

    @Test
    public void testInvalidToDateBorder() throws Exception {
        // toDate is wrong: must be lower or equals to (today + 1 month)
        // 1 month + 1 day ahead

        LocalDate fromDate = TODAY.plusDays(1);
        LocalDate toDate = TODAY.plusMonths(1).plusDays(1);
        String expectedErrorMessage = "Invalid parameter toDate '" + toDate + "'. Must be at most one moth ahead from now";
        inner_test_request_dates_error(fromDate, toDate, expectedErrorMessage);
    }

    @Test
    public void testInvalidDatesPeriod() throws Exception {
        // period is wrong: fromDate must be higher or equals to toDate
        // requesting from 20 days ago to 5 days ago

        LocalDate fromDate = TODAY.plusDays(20);
        LocalDate toDate = TODAY.plusDays(5);
        String expectedErrorMessage = "Bad parameters: fromDate must be before toDate. Can't return availability from: " + fromDate + " to: " + toDate;

        inner_test_request_dates_error(fromDate, toDate, expectedErrorMessage);
    }

    @Test
    public void testInvalidDatesPeriodBorder() throws Exception {
        // period is wrong: fromDate must be higher or equals to toDate
        // requesting from 20 days ago to 19 days ago

        LocalDate fromDate = TODAY.plusDays(20);
        LocalDate toDate = TODAY.plusDays(19);
        String expectedErrorMessage = "Bad parameters: fromDate must be before toDate. Can't return availability from: " + fromDate + " to: " + toDate;

        inner_test_request_dates_error(fromDate, toDate, expectedErrorMessage);
    }



    private void inner_test_request_dates_error(LocalDate fromDate, LocalDate toDate, String expectedErrorMessage) {

        // URI: http://localhost:8080/availability?fromDate=fromDate&toDate=toDate
        URI uri = UriBuilder.of(ENDPOINT_URL + "?fromDate=" + fromDate + "&toDate=" + toDate).build();
        MutableHttpRequest request = HttpRequest.GET(uri);
        try {
            HttpResponse<Map<String, String>> httpResponse = client.toBlocking().exchange(request, Argument.of(Map.class, String.class, String.class));
            fail("must fail");
        } catch (HttpClientResponseException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus(), "response status is wrong");
            assertNotNull(e.getResponse());
            assertNotNull(e.getResponse().getBody());
            assertTrue(e.getResponse().getBody().isPresent());
            Object errorBody = e.getResponse().getBody().get();
            assertEquals(errorBody.toString(), "{error=" + expectedErrorMessage + "}");
        }
    }


}