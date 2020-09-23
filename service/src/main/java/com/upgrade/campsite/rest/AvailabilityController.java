package com.upgrade.campsite.rest;

import io.micronaut.core.convert.format.Format;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;

import javax.annotation.Nullable;
import java.time.LocalDate;

@Controller("/availability")
public class AvailabilityController {

    @Get()
    public String availability(@QueryValue @Format("yyyy-MM-dd") @Nullable LocalDate from,
                               @QueryValue @Format("yyyy-MM-dd") @Nullable LocalDate to) {


        // verificar si today sigue siendo == LocalDate.now()
        LocalDate today = LocalDate.now();
        if (from == null) {
            from = today.plusDays(1);
        }

        if (to == null) {
            to = today.plusMonths(1);
        }

        String r = "availability :__" + from + " __to__ " + to;

        return r;
    }


}
