package com.upgrade.campsite.utils;

import com.upgrade.campsite.exception.ServiceException;
import com.upgrade.campsite.service.AvailabilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

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
        // toDate must be at most one moth ahead from now
        if (toDate.isAfter(oneMonthAhead)) {
            throw new ServiceException("Invalid parameter toDate '" + toDate + "'. Must be at most one moth ahead from now");
        }

        LOG.debug("date range is valid! [fromDate {} - toDate {}]", fromDate, toDate);
    }


}
