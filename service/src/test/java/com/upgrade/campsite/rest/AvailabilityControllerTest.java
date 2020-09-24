package com.upgrade.campsite.rest;

import com.upgrade.campsite.dto.DateAvailavility;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
@MicronautTest
class AvailabilityControllerTest {

    final String ENDPOINT_URL = "/availability";

    @Inject
    @Client("/")
    RxHttpClient client;

//    @BeforeEach
//    void setUp() {
//    }
//
//    @AfterEach
//    void tearDown() {
//    }

    @Test
    public void testDefaultDateResponse() throws Exception {
        URI uri = UriBuilder.of(ENDPOINT_URL).build();
        MutableHttpRequest request = HttpRequest.GET(uri);
        HttpResponse<List<DateAvailavility>> httpResponse = client.toBlocking().exchange(request, Argument.of(List.class, DateAvailavility.class));

        assertEquals(HttpStatus.OK, httpResponse.getStatus(), "response status is wrong");
        Optional<List<DateAvailavility>> oBody = httpResponse.getBody();
        assertTrue(oBody.isPresent(), "body is empty");


        //first mock implementation returns 10 elements
        List<DateAvailavility> responseList = oBody.get();
        assertEquals(10, responseList.size(), "response list size is wrong");

        // checking each date in list
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 10; i++) {
            DateAvailavility dateAvailavility = responseList.get(i);
            assertEquals(today.plusDays(i),dateAvailavility.getDate(),  "index " + i + ":  Date value is wrong" );
            assertEquals((i % 2 == 0),dateAvailavility.isVacant(), "index " + i + ": Vacant value is wrong" );
        }

    }

}