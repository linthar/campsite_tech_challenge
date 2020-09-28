package com.upgrade.campsite.service;

import com.upgrade.campsite.cache.RedisClient;
import com.upgrade.campsite.utils.AvailavilityConstants;
import com.upgrade.campsite.utils.DatesValidator;
import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.upgrade.campsite.utils.AvailabilityTestUtils.assertAvailabilityForDatesRange;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@MicronautTest
class AvailabilityServiceTest {

    ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    final LocalDate TODAY = LocalDate.now();

    @Inject
    AvailabilityService service;

    @Inject
    OccupiedDateService occupiedDateServiceMock;

    @MockBean(OccupiedDateService.class)
    OccupiedDateService occupiedDateServiceMock() {
        return mock(OccupiedDateService.class);
    }

    @Inject
    DatesValidator datesValidatorMock;

    @MockBean(DatesValidator.class)
    DatesValidator datesValidatorMock() {
        return mock(DatesValidator.class);
    }

    @Inject
    private RedisClient redisClient;

    @BeforeEach
    void setUp() {
        // clean up all keys from embedded REDIS
        redisClient.getRedisCommands().flushall();
    }

    // tests uses "valid" dates, dates validity are tested in DatesValidatorTest class

    @Test
    void getAvailabilityWithAllDatesVacant() {
        //check availability
        // with all dates vacant

        LocalDate fromDate = LocalDate.now().plusDays(1);
        LocalDate toDate = fromDate.plusDays(RANDOM.nextInt(1, 27));

        // There are no occupied dates in the DB (neither in Redis)
        List<LocalDate> occupiedDates = new ArrayList<>();

        Map<LocalDate, String> availability = service.getAvailability(fromDate, toDate);

        // check the availability response for the given dates range
        // with all campsite dates vacant
        assertAvailabilityForDatesRange(availability, occupiedDates, fromDate, toDate);

        // asserts that dates where checked by datesValidatorMock
        verify(datesValidatorMock, times(1)).validateAvailabilityDates(fromDate, toDate);
    }

    @Test
    void getAvailabilityWithAllDatesOccupied() {
        //check availability
        // with no vacancy (all dates are occupied)

        LocalDate fromDate = LocalDate.now().plusDays(1);
        LocalDate toDate = fromDate.plusDays(RANDOM.nextInt(1, 27));

        List<LocalDate> occupiedDates = new ArrayList<>();
        // There are occupied dates in the DB (all dates in the availability period)
        // also in REDIS
        for (LocalDate date = fromDate; date.isBefore(toDate.plusDays(1)); date = date.plusDays(1)) {
            occupiedDates.add(date);
            redisClient.storeInCache(date, UUID.randomUUID());
        }


        Map<LocalDate, String> availability = service.getAvailability(fromDate, toDate);

        // check the availability response for the given dates range
        // with all campsite dates vacant
        assertAvailabilityForDatesRange(availability, occupiedDates, fromDate, toDate);

        // asserts that dates where checked by datesValidatorMock
        verify(datesValidatorMock, times(1)).validateAvailabilityDates(fromDate, toDate);

    }

    @Test
    void getAvailabilityWithTwoDatesOccupied() {
        //check availability
        // all dates are vacant except for two (fromDate and toDate)

        LocalDate fromDate = LocalDate.now().plusDays(1);
        LocalDate toDate = fromDate.plusDays(RANDOM.nextInt(1, 27));

        // There are occupied dates in the DB also in REDIS
        List<LocalDate> occupiedDates = new ArrayList<>();
        occupiedDates.add(toDate);
        redisClient.storeInCache(toDate, UUID.randomUUID());
        occupiedDates.add(fromDate);
        redisClient.storeInCache(fromDate, UUID.randomUUID());

        Map<LocalDate, String> availability = service.getAvailability(fromDate, toDate);

        // check the availability response for the given dates range
        // with all campsite dates vacant
        assertAvailabilityForDatesRange(availability, occupiedDates, fromDate, toDate);

        // asserts that dates where checked by datesValidatorMock
        verify(datesValidatorMock, times(1)).validateAvailabilityDates(fromDate, toDate);

    }

    @Test
    void buildAvailabilityReportMapForOneDay() {
        LocalDate fromDate = TODAY.plusDays(1);
        LocalDate toDate = fromDate;
        //border case one day (fromDate == toDate)
        TreeMap<LocalDate, String> templateMap = service.buildAvailabilityReportMap(fromDate, toDate);
        assertTemplateMapForDates(templateMap, 1, fromDate, toDate);
    }

    @Test
    void buildAvailabilityReportMapForOneMonth() {
        LocalDate fromDate = TODAY.plusDays(1);
        LocalDate toDate = TODAY.plusDays(30);

        TreeMap<LocalDate, String> templateMap = service.buildAvailabilityReportMap(fromDate, toDate);
        assertTemplateMapForDates(templateMap, 30, fromDate, toDate);
    }


    @Test
    void buildAvailabilityReportMapForInvalidDates() {
        LocalDate fromDate = TODAY.plusDays(1);
        LocalDate toDate = fromDate.minusDays(10);
        //border case (fromDate is after toDate)

        TreeMap<LocalDate, String> templateMap = service.buildAvailabilityReportMap(fromDate, toDate);
        assertEquals(0, templateMap.size(), "templateMap size must be zero for invalid dates: " + fromDate + " to: " + toDate);
    }


    private void assertTemplateMapForDates(TreeMap<LocalDate, String> templateMap, int expectedSize, LocalDate fromDate, LocalDate toDate) {

        assertEquals(expectedSize, templateMap.size(), "templateMap size is wrong for dates from: " + fromDate + " to: " + toDate);

        // check each day between fromDate and toDate dates is present in the response
        for (LocalDate date = fromDate; date.isBefore(toDate.plusDays(1)); date = date.plusDays(1)) {
            String dateAvailavilty = templateMap.get(date);
            // checking each date exists in the map
            assertNotNull(dateAvailavilty, date + " availability is missing");
            assertEquals(AvailavilityConstants.NOT_OCCUPIED_DATE, dateAvailavilty, date + " availability value is wrong");
        }
    }

    @Test
    void isAvailableBetweenDatesWhenNoOccupiedDatesI() {

        LocalDate fromDate = TODAY.plusDays(RANDOM.nextInt(1, 10));
        LocalDate toDate = fromDate.plusDays(2);

        // there are no occupied dates in REDIS
        assertTrue(service.isAvailableBetweenDates(fromDate, toDate));
    }

    @Test
    void isAvailableBetweenDatesOneDateBeginning() {

        LocalDate fromDate = TODAY.plusDays(RANDOM.nextInt(1, 10));
        LocalDate toDate = fromDate.plusDays(2);

        // one of the date (at the Beginning of the period) are marked as occupied in REDIS
        redisClient.storeInCache(fromDate, UUID.randomUUID());

        assertFalse(service.isAvailableBetweenDates(fromDate, toDate));
    }

    @Test
    void isAvailableBetweenDatesOneDateMiddle() {

        LocalDate fromDate = TODAY.plusDays(RANDOM.nextInt(1, 10));
        LocalDate toDate = fromDate.plusDays(2);

        // one of the date (in the Middle of the period) are marked as occupied in REDIS
        redisClient.storeInCache(fromDate.plusDays(1), UUID.randomUUID());

        assertFalse(service.isAvailableBetweenDates(fromDate, toDate));
    }

    @Test
    void isAvailableBetweenDatesOneDateEnd() {

        LocalDate fromDate = TODAY.plusDays(RANDOM.nextInt(1, 10));
        LocalDate toDate = fromDate.plusDays(2);

        // one of the date (in the End of the period) are marked as occupied in REDIS
        redisClient.storeInCache(toDate, UUID.randomUUID());

        assertFalse(service.isAvailableBetweenDates(fromDate, toDate));
    }


    @Test
    void isAvailableBetweenDatesOutside() {

        LocalDate fromDate = TODAY.plusDays(RANDOM.nextInt(1, 10));
        LocalDate toDate = fromDate.plusDays(2);

        // there are dates marked as occupied in REDIS
        // but outside the period (Before or/and After)
        redisClient.storeInCache(fromDate.minusDays(1), UUID.randomUUID());
        redisClient.storeInCache(toDate.plusDays(1), UUID.randomUUID());

        assertTrue(service.isAvailableBetweenDates(fromDate, toDate));
    }



}