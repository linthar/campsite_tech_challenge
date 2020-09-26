package com.upgrade.campsite.service;

import static com.upgrade.campsite.utils.AvailavilityUtils.*;
import com.upgrade.campsite.model.OccupiedDate;
import com.upgrade.campsite.repository.OccupiedDateRepository;
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

    @Inject
    private OccupiedDateService occupiedDateService;

    public Map<LocalDate, String> getAvailability(@NotNull LocalDate fromDate, @NotNull LocalDate toDate) {
        LOG.debug("searching getAvailability for [fromDate: {} - toDate: {}]", fromDate, toDate);
        validateDates(fromDate, toDate);

        LOG.debug("finding occupied dates into DB "); //in the provided date range
        List<OccupiedDate> occupiedDates = occupiedDateService.findAllBetweenDates(fromDate, toDate);

        // TreeMap is ordered
        // the response will be ordered by date descending
        TreeMap<LocalDate, String> response = buildResponseMapTemplate(fromDate, toDate);
        for (OccupiedDate occupiedDate: occupiedDates) {
            response.put(occupiedDate.getDate(), OCCUPIED_DATE);
        }
        return response;
    }


}
