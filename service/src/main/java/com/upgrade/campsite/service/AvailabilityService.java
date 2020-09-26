package com.upgrade.campsite.service;

import com.upgrade.campsite.exception.ServiceException;
import com.upgrade.campsite.model.OccupiedDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Singleton
public class AvailabilityService {

    private static final Logger LOG = LoggerFactory.getLogger(AvailabilityService.class);

    public static final String OCCUPIED_DATE = "taken";
    public static final String NOT_OCCUPIED_DATE = "vacant";

    @Inject
    private OccupiedDateService occupiedDateService;

    public Map<LocalDate, String> getAvailability(@NotNull LocalDate fromDate, @NotNull LocalDate toDate) {
        LOG.debug("searching getAvailability for [fromDate: {} - toDate: {}]", fromDate, toDate);
        validateDates(fromDate, toDate);

        LOG.debug("finding occupied dates into DB "); //in the provided date range
        List<OccupiedDate> occupiedDates = occupiedDateService.findAllBetweenDates(fromDate, toDate);

        // response is a map with all dates between fromDate and toDate as vacant
        // (TreeMap is ordered) Json response will be ordered by date descending
        TreeMap<LocalDate, String> response = buildResponseMapTemplate(fromDate, toDate);

        // now we have to replace all occupiedDates as taken in the response template map
        occupiedDates.stream().forEach(occupiedDate -> response.put(occupiedDate.getDate(), OCCUPIED_DATE));

        return response;
    }


    /**
     * Builds a map with all dates between fromDate and toDate as vacant
     * (TreeMap is ordered) the response will be ordered by date descending
     * <p>
     * e.g.:
     * buildResponseMapTemplate(2020-10-16, 2020-10-18)
     * returns:
     * {
     * "2020-10-16": "vacant",
     * "2020-10-17": "vacant",
     * "2020-10-18": "vacant",
     * }
     *
     * @param fromDate first date in the map
     * @param toDate   last date in the map
     * @return a TreeMap
     */
    protected TreeMap<LocalDate, String> buildResponseMapTemplate(@NotNull LocalDate fromDate, @NotNull LocalDate toDate) {

        TreeMap<LocalDate, String> template = new TreeMap<LocalDate, String>();
        LocalDate loopLimit = toDate.plusDays(1);
        for (LocalDate date = fromDate; date.isBefore(loopLimit); date = date.plusDays(1)) {
            template.put(date, NOT_OCCUPIED_DATE);
        }
        return template;
    }

    /**
     * Check if dates are valid
     * fromDate must be lower or equals to toDate
     * fromDate must be higher than today
     * toDate must be lower or equals to  [today + 1 month]
     *
     * @param fromDate from date to check
     * @param toDate   to date to check
     */
    protected void validateDates(@NotNull LocalDate fromDate, @NotNull LocalDate toDate) {
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
