package com.upgrade.campsite.service;

import com.upgrade.campsite.exception.ServiceException;
import com.upgrade.campsite.model.OccupiedDate;
import com.upgrade.campsite.utils.DatesValidator;
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

    @Inject
    private DatesValidator datesValidator;

    @Inject
    private RedisClient redisClient;

    public Map<LocalDate, String> getAvailability(@NotNull LocalDate fromDate, @NotNull LocalDate toDate) {
        LOG.debug("searching getAvailability for [fromDate: {} - toDate: {}]", fromDate, toDate);
        datesValidator.validateAvailabilityDates(fromDate, toDate);

        LOG.debug("finding occupied dates into DB "); //in the provided date range
        List<OccupiedDate> occupiedDates = occupiedDateService.findAllBetweenDates(fromDate, toDate);

        // response is a map with all dates between fromDate and toDate as vacant
        // (TreeMap is ordered) Json response will be ordered by date descending
        TreeMap<LocalDate, String> response = buildAvailabilityReportMapTemplate(fromDate, toDate);


        // now we have to replace all occupiedDates as taken in the response template map
        occupiedDates.stream().forEach(occupiedDate -> response.put(occupiedDate.getDate(), OCCUPIED_DATE));

        return response;
    }


    /**
     * Builds a map with all dates between fromDate and toDate as vacant
     * (TreeMap is ordered) the response will be ordered by date descending
     * <p>
     * e.g.:
     * buildAvailabilityReportMapTemplate(2020-10-16, 2020-10-18)
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
    protected TreeMap<LocalDate, String> buildAvailabilityReportMapTemplate(@NotNull LocalDate fromDate, @NotNull LocalDate toDate) {

        TreeMap<LocalDate, String> template = new TreeMap<LocalDate, String>();
        LocalDate loopLimit = toDate.plusDays(1);
        for (LocalDate date = fromDate; date.isBefore(loopLimit); date = date.plusDays(1)) {
            template.put(date, NOT_OCCUPIED_DATE);
        }
        return template;
    }


}
