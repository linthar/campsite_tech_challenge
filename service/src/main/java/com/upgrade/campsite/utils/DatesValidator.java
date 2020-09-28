package com.upgrade.campsite.utils;

import com.upgrade.campsite.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Singleton
public class DatesValidator {

    private static final Logger LOG = LoggerFactory.getLogger(DatesValidator.class);

    /**
     * Check if dates are valid to get the Availability report
     * fromDate must be lower or equals to toDate
     * fromDate must be higher than today
     * toDate must be lower or equals to  [today + 1 month]
     *
     * @param fromDate from date to check
     * @param toDate   to date to check
     */
    public void validateAvailabilityDates(@NotNull LocalDate fromDate, @NotNull LocalDate toDate) {
        LOG.debug("checking date range for availability report [fromDate {} - toDate {}]", fromDate, toDate);

        // vaidate if from & to range is Ok
        if (toDate.isBefore(fromDate)) {
            throw new ServiceException("Bad parameters: fromDate must be before toDate. Can't return availability from: " + fromDate + " to: " + toDate);
        }

        //dates range check
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        // fromDate must be at least tomorrow or ahead
        if (fromDate.isBefore(tomorrow)) {
            throw new ServiceException("Invalid parameter fromDate '" + fromDate + "'. Must be at least tomorrow or ahead");

        }

        LocalDate oneMonthAhead = today.plusMonths(1);
        // toDate must be at most one month ahead from now
        if (toDate.isAfter(oneMonthAhead)) {
            throw new ServiceException("Invalid parameter toDate '" + toDate + "'. Must be at most one month ahead from now");
        }

        LOG.debug("date range is valid! [fromDate {} - toDate {}]", fromDate, toDate);
    }



    /**
     * Check if dates are valid for reserve the campsite
     * <p>
     * The campsite can be reserved for max 3 days.
     * The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.
     *
     * @param arrivalDate   reservation arrival date
     * @param departureDate reservation departure date
     */
    public void validateReservationDates(LocalDate arrivalDate, LocalDate departureDate) {
        LOG.debug("checking reservation dates [arrivalDate {} - departureDate {}]", arrivalDate, departureDate);


        // arrivalDate <= departureDate
        if (departureDate.isBefore(arrivalDate)) {
            throw new ServiceException("arrivalDate must be before (or equals) to departureDate");
        }

        long stayDays = ChronoUnit.DAYS.between(arrivalDate, departureDate) + 1;
        if (stayDays > 3) {
            throw new ServiceException("The campsite can be reserved for max 3 days");
        }

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        if (arrivalDate.isBefore(tomorrow)) {
            throw new ServiceException("The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance");
        }

        LocalDate nextMonthDate = today.plusMonths(1);
        if (departureDate.isAfter(nextMonthDate)) {
            throw new ServiceException("The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance");
        }

        LOG.debug("checking reservation dates are valid! [arrivalDate {} - departureDate {}]", arrivalDate, departureDate);
    }



}
