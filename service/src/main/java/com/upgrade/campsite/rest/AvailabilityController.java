package com.upgrade.campsite.rest;

import com.upgrade.campsite.service.AvailabilityService;
import io.micronaut.core.convert.format.Format;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.validation.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Controller("/availability")
@Validated
public class AvailabilityController {

    private static final Logger LOG = LoggerFactory.getLogger(AvailabilityController.class);

    @Inject
    private AvailabilityService service;

    @Get()
    public Map<LocalDate, String> availability(@QueryValue @Format("yyyy-MM-dd") @Nullable LocalDate fromDate,
                                                   @QueryValue @Format("yyyy-MM-dd") @Nullable LocalDate toDate) {

        //Endpoint Default values
        LocalDate today = LocalDate.now();
        if (fromDate == null) {
            fromDate = today.plusDays(1);
            LOG.debug("setting fromDate as default value: {}", fromDate );
        }
        if (toDate == null) {
            toDate = today.plusMonths(1);
            LOG.debug("setting toDate as default value: {}", toDate);
        }

        return service.getAvailability(fromDate, toDate);
    }



}
