package com.upgrade.campsite.service;

import com.upgrade.campsite.model.OccupiedDate;
import com.upgrade.campsite.utils.AvailavilityConstants;
import com.upgrade.campsite.utils.DatesValidator;
import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.upgrade.campsite.utils.AvailabilityTestUtils.assertAvailabilityForDatesRange;
import static com.upgrade.campsite.utils.AvailabilityTestUtils.toLocalDateList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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


    // tests use "valid" dates, dates validity are tested in DatesValidatorTest class

    //getAvailability() tests
    //////////////////////////////////

    @Test
    void getAvailabilityWithAllDatesVacant() {
        //check availability
        // with all dates vacant

        LocalDate fromDate = LocalDate.now().plusDays(1);
        LocalDate toDate = fromDate.plusDays(RANDOM.nextInt(1, 27));

        List<OccupiedDate> occupiedDates = new ArrayList<>();
        // There are no occupied dates in the DB
        when(occupiedDateServiceMock.findAllBetweenDates(fromDate, toDate)).thenReturn(occupiedDates);

        Map<LocalDate, String> availability = service.getAvailability(fromDate, toDate);

        // check the availability response for the given dates range
        // with all campsite dates vacant
        assertAvailabilityForDatesRange(availability, toLocalDateList(occupiedDates), fromDate, toDate);

        // asserts that dates where checked by datesValidatorMock
        verify(datesValidatorMock, times(1)).validateAvailabilityDates(fromDate, toDate);
    }

    @Test
    void getAvailabilityWithAllDatesOccupied() {
        //check availability
        // with no vacancy (all dates are occupied)

        LocalDate fromDate = LocalDate.now().plusDays(1);
        LocalDate toDate = fromDate.plusDays(RANDOM.nextInt(1, 27));

        List<OccupiedDate> occupiedDates = new ArrayList<>();
        for (LocalDate date = fromDate; date.isBefore(toDate.plusDays(1)); date = date.plusDays(1)) {
            occupiedDates.add(new OccupiedDate(date, UUID.randomUUID()));
        }

        // There are occupied dates in the DB (all dates in the availability period
        when(occupiedDateServiceMock.findAllBetweenDates(fromDate, toDate)).thenReturn(occupiedDates);

        Map<LocalDate, String> availability = service.getAvailability(fromDate, toDate);

        // check the availability response for the given dates range
        // with all campsite dates vacant
        assertAvailabilityForDatesRange(availability, toLocalDateList(occupiedDates), fromDate, toDate);

        // asserts that dates where checked by datesValidatorMock
        verify(datesValidatorMock, times(1)).validateAvailabilityDates(fromDate, toDate);

    }

    @Test
    void getAvailabilityWithTwoDatesOccupied() {
        //check availability
        // all dates are vacant except for two (fromDate and toDate)

        LocalDate fromDate = LocalDate.now().plusDays(1);
        LocalDate toDate = fromDate.plusDays(RANDOM.nextInt(1, 27));

        List<OccupiedDate> occupiedDates = new ArrayList<>();
        occupiedDates.add(new OccupiedDate(toDate, UUID.randomUUID()));
        occupiedDates.add(new OccupiedDate(fromDate, UUID.randomUUID()));

        // There are two occupied dates in the DB (all dates in the availability period
        when(occupiedDateServiceMock.findAllBetweenDates(fromDate, toDate)).thenReturn(occupiedDates);

        Map<LocalDate, String> availability = service.getAvailability(fromDate, toDate);

        // check the availability response for the given dates range
        // with all campsite dates vacant
        assertAvailabilityForDatesRange(availability, toLocalDateList(occupiedDates), fromDate, toDate);

        // asserts that dates where checked by datesValidatorMock
        verify(datesValidatorMock, times(1)).validateAvailabilityDates(fromDate, toDate);

    }


    /// buildAvailabilityReportMapTemplate() tests
    //////////////////////////////////

    @Test
    void buildAvailabilityReportMapTemplateForOneDay() {
        LocalDate fromDate = TODAY.plusDays(1);
        LocalDate toDate = fromDate;
        //border case one day (fromDate == toDate)
        TreeMap<LocalDate, String> templateMap = service.buildAvailabilityReportMapTemplate(fromDate, toDate);
        assertTemplateMapForDates(templateMap, 1, fromDate, toDate);
    }

    @Test
    void buildAvailabilityReportMapTemplateForOneMonth() {
        LocalDate fromDate = TODAY.plusDays(1);
        LocalDate toDate = TODAY.plusDays(30);

        TreeMap<LocalDate, String> templateMap = service.buildAvailabilityReportMapTemplate(fromDate, toDate);
        assertTemplateMapForDates(templateMap, 30, fromDate, toDate);
    }


    @Test
    void buildAvailabilityReportMapTemplateForInvalidDates() {
        LocalDate fromDate = TODAY.plusDays(1);
        LocalDate toDate = fromDate.minusDays(10);
        //border case (fromDate is after toDate)

        TreeMap<LocalDate, String> templateMap = service.buildAvailabilityReportMapTemplate(fromDate, toDate);
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


}