package com.upgrade.campsite.rest;

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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
@MicronautTest
class AvailabilityControllerTest {

    final String ENDPOINT_URL = "/availability";

    @Inject
    @Client("/")
    RxHttpClient client;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }


    @Test
    public void testDefaultDateResponse() throws Exception {
        URI uri = UriBuilder.of(ENDPOINT_URL).build();
        MutableHttpRequest request = HttpRequest.GET(uri);
        HttpResponse<String> response = client.toBlocking().exchange(request, String.class);

        assertEquals(HttpStatus.OK, response.getStatus(), "response status is wrong");

        Optional<String> oBody = response.getBody();
        assertTrue(oBody.isPresent(), "body is empty");

        // verificar si today sigue siendo == LocalDate.now()
        LocalDate today = LocalDate.now();
        LocalDate from = today.plusDays(1);
        LocalDate to = today.plusMonths(1);

        String expected = "availability :__" + from + " __to__ " + to;
        assertEquals(expected, oBody.get(), "response body is wrong");
    }


}