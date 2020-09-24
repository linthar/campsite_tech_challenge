package com.upgrade.campsite.rest;

import com.upgrade.campsite.dto.DateAvailavility;
import io.micronaut.core.convert.format.Format;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.validation.Validated;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller("/availability")
@Validated
public class AvailabilityController {

    @Get()
    public List<DateAvailavility> availability(@QueryValue @Format("yyyy-MM-dd") @Nullable LocalDate from,
                                               @QueryValue @Format("yyyy-MM-dd") @Nullable LocalDate to) {


        // first mock
        // verificar si today sigue siendo == LocalDate.now()
        LocalDate today = LocalDate.now();
        if (from == null) {
            from = today.plusDays(1);
        }

        if (to == null) {
            to = today.plusMonths(1);
        }

        //response Mock
        List<DateAvailavility> responseMock = new ArrayList<DateAvailavility>(10);
        for (int i = 0; i < 10 ; i++) {
            responseMock.add(new DateAvailavility(today.plusDays(i), (i % 2 == 0)));
        }
        return responseMock;
    }


}
