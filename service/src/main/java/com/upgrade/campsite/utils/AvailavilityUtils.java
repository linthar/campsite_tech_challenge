package com.upgrade.campsite.utils;

import com.upgrade.campsite.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.TreeMap;

public class AvailavilityUtils {

    private static final Logger LOG = LoggerFactory.getLogger(AvailavilityUtils.class);

    public static final String OCCUPIED_DATE = "taken";
    public static final String NOT_OCCUPIED_DATE = "vacant";

    public static String getStringValue(Boolean isOccupied) {
        return isOccupied ? OCCUPIED_DATE : NOT_OCCUPIED_DATE;
    }

    public static TreeMap<LocalDate, String> buildResponseMapTemplate(@NotNull LocalDate fromDate, @NotNull LocalDate toDate) {

        TreeMap<LocalDate, String> template = new TreeMap<LocalDate, String>();
        LocalDate loopLimit = toDate.plusDays(1);
        for (LocalDate date = fromDate; date.isBefore(loopLimit); date = date.plusDays(1)) {
            template.put(date, NOT_OCCUPIED_DATE);
        }
        return template;
    }

    public static void validateDates(@NotNull LocalDate fromDate, @NotNull LocalDate toDate) {
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
