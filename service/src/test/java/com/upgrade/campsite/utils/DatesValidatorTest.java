package com.upgrade.campsite.utils;

import com.upgrade.campsite.exception.ServiceException;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@MicronautTest
class DatesValidatorTest {

    @Inject
    private DatesValidator datesValidator;

    ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    final LocalDate TODAY = LocalDate.now();

    @Test
    void validateAvailabilityDatesOk() {

        LocalDate fromDate = TODAY.plusDays(RANDOM.nextInt(1, 10));
        LocalDate toDate = fromDate.plusDays(RANDOM.nextInt(5, 10));

        datesValidator.validateAvailabilityDates(fromDate, toDate);
    }

    @Test
    void validateAvailabilityDatesOkTomorrow() {
        LocalDate fromDate = TODAY.plusDays(1); //tomorrow
        LocalDate toDate = fromDate; //tomorrow

        datesValidator.validateAvailabilityDates(fromDate, toDate);
    }


    @Test
    void validateAvailabilityDatesOkNextMonth() {
        LocalDate fromDate = TODAY.plusMonths(1); // today + 1 month ahead
        LocalDate toDate = fromDate;
        datesValidator.validateAvailabilityDates(fromDate, toDate);
    }

    @Test
    void validateAvailabilityDatesOkMaxPeriod() {
        LocalDate fromDate = TODAY.plusDays(1); //tomorrow
        LocalDate toDate = TODAY.plusMonths(1); // today + 1 month ahead

        datesValidator.validateAvailabilityDates(fromDate, toDate);
    }

    @Test
    void validateAvailabilityDatesInverseDatesPeriod() {
        LocalDate fromDate = TODAY.plusMonths(1); // today + 1 month ahead
        LocalDate toDate = TODAY.plusDays(1); //tomorrow

        try {
            datesValidator.validateAvailabilityDates(fromDate, toDate);
        } catch (ServiceException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("Bad parameters: fromDate must be before toDate. Can't return availability from: " +
                    fromDate + " to: " + toDate, e.getMessage());
        }
    }

    @Test
    void validateAvailabilityDatesInvalidFromDate() {
        LocalDate fromDate = TODAY; //invalid
        LocalDate toDate = TODAY.plusDays(5); // today + 5 days (valid)

        try {
            datesValidator.validateAvailabilityDates(fromDate, toDate);
            fail("must fail");
        } catch (ServiceException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("Invalid parameter fromDate '" + fromDate + "'. Must be at least tomorrow or ahead",
                    e.getMessage());
        }
    }

    @Test
    void validateAvailabilityDatesInvalidToDate() {
        LocalDate fromDate = TODAY.plusDays(1); //tomorrow
        LocalDate toDate = fromDate.plusMonths(1); // tomorrow + 1 month ahead (invalid)

        try {
            datesValidator.validateAvailabilityDates(fromDate, toDate);
            fail("must fail");
        } catch (ServiceException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("Invalid parameter toDate '" + toDate + "'. Must be at most one month ahead from now",
                    e.getMessage());
        }
    }


    @Test
    void validateReservationDatesOkOneDayPeriod() {
        LocalDate arrivalDate = TODAY.plusDays(1);
        LocalDate departureDate = TODAY.plusDays(1);

        datesValidator.validateReservationDates(arrivalDate, departureDate);
    }

    @Test
    void validateReservationDatesOkTwoDaysPeriod() {
        LocalDate arrivalDate = TODAY.plusDays(1);
        LocalDate departureDate = TODAY.plusDays(2);

        datesValidator.validateReservationDates(arrivalDate, departureDate);
    }

    @Test
    void validateReservationDatesOkThreeDaysPeriod() {
        LocalDate arrivalDate = TODAY.plusDays(1);
        LocalDate departureDate = TODAY.plusDays(3);

        datesValidator.validateReservationDates(arrivalDate, departureDate);
    }

    @Test
    void validateReservationDatesErrorFourDaysPeriod() {
        LocalDate arrivalDate = TODAY.plusDays(1);
        LocalDate departureDate = TODAY.plusDays(4);
        try {
            datesValidator.validateReservationDates(arrivalDate, departureDate);
        } catch (ServiceException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("The campsite can be reserved for max 3 days", e.getMessage());
        }
    }

    @Test
    void validateReservationDatesErrorArrivalAfterDeparture() {
        LocalDate arrivalDate = TODAY.plusDays(10);
        LocalDate departureDate = arrivalDate.minusDays(2);
        //departure < arrival
        try {
            datesValidator.validateReservationDates(arrivalDate, departureDate);
        } catch (ServiceException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("arrivalDate must be before (or equals) to departureDate", e.getMessage());
        }
    }


    @Test
    void validateReservationDatesErrorTooFarInAdvance() {
        //trying to made a reservation too far in advance
        LocalDate arrivalDate = TODAY.plusMonths(1).plusDays(1);
        LocalDate departureDate = arrivalDate.plusDays(1);
        try {
            datesValidator.validateReservationDates(arrivalDate, departureDate);
        } catch (ServiceException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance", e.getMessage());
        }
    }

    @Test
    void validateReservationDatesErrorToday() {
        //trying to made a reservation for today
        LocalDate arrivalDate = TODAY;
        LocalDate departureDate = arrivalDate.plusDays(1);
        try {
            datesValidator.validateReservationDates(arrivalDate, departureDate);
        } catch (ServiceException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance", e.getMessage());
        }
    }


}