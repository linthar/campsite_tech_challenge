package com.upgrade.campsite.service;

import com.upgrade.campsite.cache.RedisClient;
import com.upgrade.campsite.repository.OccupiedDateRepository;
import com.upgrade.campsite.repository.ReservationRepository;
import com.upgrade.campsite.rest.dto.ReservationRequest;
import com.upgrade.campsite.utils.ReservationTestUtils;
import io.micronaut.http.resource.$ResourceLoaderFactory$GetClassPathResourceLoader0DefinitionClass;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import static org.junit.jupiter.api.Assertions.*;
@MicronautTest
class ConcurrentCreateReservationServiceConstraintsTest {

    @Inject
    protected ReservationService service;

    @Inject
    protected OccupiedDateRepository occupiedDateRepository;

    @Inject
    protected AvailabilityService availabilityService;

    @Inject
    protected ReservationRepository reservationRepository;

    @Inject
    protected RedisClient redisClient;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
        // clean up all keys from embedded REDIS
        redisClient.getRedisCommands().flushall();

        // starts with an empty DB to avoid false negative in tests
        // (reservation dates are random)
        occupiedDateRepository.deleteAll();
        reservationRepository.deleteAll();
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
        final Map<Integer, Exception> failuresMap = new HashMap<>();
        final Integer[] successIndexHolder =  {-1};


        Map<LocalDate, String> availabilityReportBefore = availabilityService.getAvailability(dateToCompite.minusDays(3), dateToCompite.plusDays(3));

        // all dates must be free before the test
        for (String value :availabilityReportBefore.values()) {
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
                    createReservationRequest(iCopy, reservationList);
                    successIndexHolder[0] = iCopy;
                } catch (Exception e) {
                    failuresMap.put(iCopy, e);
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
        List<LocalDate> succeededDates = service.createDatesBetweenList(succeededReservation.getArrivalDate(), succeededReservation.getDepartureDate());

        for (Map.Entry<LocalDate, String> entry: availabilityReport.entrySet()) {
            if (succeededDates.contains(entry.getKey())) {
                assertEquals(AvailabilityService.OCCUPIED_DATE, entry.getValue(), "availability check error for date "+entry.getKey());
            } else {
                assertEquals(AvailabilityService.NOT_OCCUPIED_DATE, entry.getValue(), "availability check error for date "+entry.getKey());
            }
        }
    }

    void createReservationRequest(int index, List<ReservationRequest> reservationList) {
        ReservationRequest r = reservationList.get(index);
        service.create(r.getEmail(), r.getFullname(), r.getArrivalDate(), r.getDepartureDate());
    }




}