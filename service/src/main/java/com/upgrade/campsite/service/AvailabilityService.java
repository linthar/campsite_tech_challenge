package com.upgrade.campsite.service;

import com.upgrade.campsite.cache.RedisClient;
import com.upgrade.campsite.utils.DatesValidator;
import io.micronaut.validation.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

@Singleton
@Validated
public class AvailabilityService {

    private static final Logger LOG = LoggerFactory.getLogger(AvailabilityService.class);

    public static final String OCCUPIED_DATE = "taken";
    public static final String NOT_OCCUPIED_DATE = "vacant";

    @Inject
    private DatesValidator datesValidator;

    @Inject
    private RedisClient redisClient;

    public Map<LocalDate, String> getAvailability(@NotNull LocalDate fromDate, @NotNull LocalDate toDate) {
        LOG.debug("searching getAvailability for [fromDate: {} - toDate: {}]", fromDate, toDate);
        datesValidator.validateAvailabilityDates(fromDate, toDate);

        // response is a map with all dates between fromDate and toDate
        // (TreeMap is ordered) Json response will be ordered by date descending
        return buildAvailabilityReportMap(fromDate, toDate);
    }


    /**
     * Builds a map with all dates between fromDate and toDate as keys and vacancy status as value
     * (TreeMap is ordered) the response will be ordered by date descending
     * <p>
     * the vacancy status provided by getAvailabilityForDateInCache(date) (checks in REDIS cached occupied dates)
     *
     * <p>
     * e.g.:
     * buildAvailabilityReportMapTemplate(2020-10-16, 2020-10-18)
     * returns:
     * {
     * "2020-10-16": "vacant",
     * "2020-10-17": "taken",
     * "2020-10-18": "vacant",
     * }
     *
     * @param fromDate first date in the map
     * @param toDate   last date in the map
     * @return a TreeMap
     */
    protected TreeMap<LocalDate, String> buildAvailabilityReportMap(@NotNull LocalDate fromDate, @NotNull LocalDate toDate) {

        TreeMap<LocalDate, String> template = new TreeMap<LocalDate, String>();
        LocalDate loopLimit = toDate.plusDays(1);
        for (LocalDate date = fromDate; date.isBefore(loopLimit); date = date.plusDays(1)) {
            //find in cache if date is available or taken
            // at most 31 individual checks will be made
            template.put(date, getAvailabilityForDateInCache(date));
        }
        return template;
    }


    /**
     * Checks the cache (Redis) to return the availality value
     * (stored OccupiedDates in DB are cached in REDIS)
     *
     * @param date date to check in cache
     * @return NOT_OCCUPIED_DATE if date (key) is missing in cache
     * OCCUPIED_DATE if date (key) was found in cache
     */
    private String getAvailabilityForDateInCache(@NotNull LocalDate date) {
        return redisClient.getFromCache(date) == null ? NOT_OCCUPIED_DATE : OCCUPIED_DATE;
    }

    /**
     * Checks if the Campsite is available for the given dates period (inclusive)
     *
     * @param fromDate first date to check availability
     * @param toDate   last date to check availability
     * @return true if all dates between newArrival and newDeparture are vacant
     */
    public boolean isAvailableBetweenDates(@NotNull LocalDate fromDate, @NotNull LocalDate toDate) {
        LocalDate loopLimit = toDate.plusDays(1);
        for (LocalDate date = fromDate; date.isBefore(loopLimit); date = date.plusDays(1)) {
            if (redisClient.getFromCache(date) != null) {
                //at least one day in the requested period is taken
                return false;
            }
        }

        return true;
    }
}
