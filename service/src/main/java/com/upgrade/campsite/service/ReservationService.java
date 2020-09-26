package com.upgrade.campsite.service;

import com.upgrade.campsite.dto.ReservationRequest;
import com.upgrade.campsite.exception.ServiceException;
import com.upgrade.campsite.model.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Singleton
@Transactional
public class ReservationService {
    private static final Logger LOG = LoggerFactory.getLogger(ReservationService.class);

    @Inject
    private OccupiedDateService occupiedDateService;

    public Optional<Reservation> create(@NotNull @Valid ReservationRequest reservation) {

        validateDates(reservation.getArrivalDate(), reservation.getDepartureDate());


        return getReservationMock(UUID.randomUUID(), reservation);
    }


    public Optional<Reservation> get(@NotNull UUID id) {
        return geMock(id);
    }

    public Optional<Reservation> update(@NotNull UUID id, @NotNull @Valid ReservationRequest reservation) {
        return getReservationMock(id, reservation);
    }

    public void delete(@NotNull UUID id) {
    }


    private Optional<Reservation> getReservationMock(UUID id, ReservationRequest reservationRequest) {
        Reservation r = new Reservation();
        r.setId(id);
        r.setEmail(reservationRequest.getEmail());
        r.setFullname(reservationRequest.getFullname());
        r.setArrivalDate(reservationRequest.getArrivalDate());
        r.setDepartureDate(reservationRequest.getDepartureDate());
        return Optional.of(r);
    }

    private Optional<Reservation> geMock(@NotNull UUID id) {
        Reservation r = new Reservation();
        r.setId(id);
        r.setEmail("aa@bb.com");
        r.setFullname("John Smith");
        LocalDate today = LocalDate.now();
        r.setArrivalDate(today.plusDays(1));
        r.setDepartureDate(today.plusDays(2));
        return Optional.of(r);
    }





    /**
     * Check if dates are valid for reserve the campsite
     *
     * The campsite can be reserved for max 3 days.
     * The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.
     *
     * @param arrivalDate reservation arrival date
     * @param departureDate reservation departure date
     */
    private void validateDates(LocalDate arrivalDate, LocalDate departureDate) {
        LOG.debug("checking reservation dates [arrivalDate {} - departureDate {}]", arrivalDate, departureDate);


        // arrivalDate <= departureDate
        if (departureDate.isBefore(arrivalDate)) {
            throw new ServiceException("arrivalDate must be before (or equals) to departureDate");
        }

        long stayDays = ChronoUnit.DAYS.between(arrivalDate, departureDate) + 1;
        if (stayDays > 3) {
            throw new ServiceException("You can stay up to 3 days");
        }

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        if (arrivalDate.isBefore(tomorrow)) {
            throw new ServiceException("The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.");
        }

        LocalDate nextMonthDate = today.plusMonths(1);
        if (departureDate.isAfter(nextMonthDate)) {
            throw new ServiceException("The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.");
        }

        LOG.debug("checking reservation dates are valid! [arrivalDate {} - departureDate {}]", arrivalDate, departureDate);
    }

}
