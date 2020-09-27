package com.upgrade.campsite.utils;

import com.upgrade.campsite.model.OccupiedDate;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AvailabilityTestUtils {


    /**
     * returns a list of List<LocalDate> containig each LocalDate in occupiedDates list
     *
     * @param occupiedDates   list of dates (OccupiedDate) to extract the LocalDate
     */
    public static List<LocalDate> toLocalDateList(List<OccupiedDate> occupiedDates) {

        return occupiedDates.stream()
               .map(occupiedDate -> {return occupiedDate.getDate();})
               .collect(Collectors.toList());
    }

    /**
     * Asserts that availabilityMap contains all dates between fromDate and toDate
     * dates are settled as NOT_OCCUPIED_DATE  except if the date is in occupiedDates list
     *
     * @param availabilityMap campsite availability map (key: date,  value: OCCUPIED_DATE|NOT_OCCUPIED_DATE)
     * @param occupiedDates   list of dates (LocalDate) when the campsite is OCCUPIED
     * @param fromDate        first date in the availability map
     * @param toDate          last date in the availability map
     */
    public static void assertAvailabilityForDatesRange(Map<LocalDate, String> availabilityMap, List<LocalDate> occupiedDates, LocalDate fromDate, LocalDate toDate) {

        // must return noOfDaysBetween dates
        long expectedSize = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        assertEquals(expectedSize, availabilityMap.size(), "response size is wrong for dates from: " + fromDate + " to: " + toDate);

        // check each day between fromDate and toDate dates is present in the response
        for (LocalDate date = fromDate; date.isBefore(toDate.plusDays(1)); date = date.plusDays(1)) {
            String dateAvailabilty = availabilityMap.get(date);
            // checking each date exists in the map
            assertNotNull(dateAvailabilty, date + " availability is missing");

            // checking each date availability value
            if (occupiedDates.contains(date)) {
                assertEquals(AvailavilityConstants.OCCUPIED_DATE, dateAvailabilty, date + " availability value is wrong");
            } else {
                assertEquals(AvailavilityConstants.NOT_OCCUPIED_DATE, dateAvailabilty, date + " availability value is wrong");
            }
        }
    }


}
