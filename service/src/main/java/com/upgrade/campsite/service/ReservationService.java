package com.upgrade.campsite.service;

import com.upgrade.campsite.dto.ReservationRequest;
import com.upgrade.campsite.exception.ServiceException;
import com.upgrade.campsite.model.Reservation;
import com.upgrade.campsite.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
@Transactional
public class ReservationService {
    private static final Logger LOG = LoggerFactory.getLogger(ReservationService.class);

    @Inject
    private OccupiedDateService occupiedDateService;

    @Inject
    private ReservationRepository repository;

    public Reservation create(@NotNull @Email String email, @NotBlank String fullname,
                              @NotNull LocalDate arrival, @NotNull LocalDate departure) {

        validateDates(arrival, departure);

        if (occupiedDateService.existAnyBetweenDates(arrival, departure)) {
            throw new ServiceException("provided dates period is no free, check please check availability for details");
        }


        Reservation entity = new Reservation();
        UUID reservationId = UUID.randomUUID();
        entity.setId(reservationId);
        entity.setEmail(email);
        entity.setFullname(fullname);
        entity.setArrivalDate(arrival);
        entity.setDepartureDate(departure);

        return saveReservationAndOccupiedDates(entity);
    }


    public Optional<Reservation> findByID(@NotNull UUID id) {
        return repository.findById(id);
    }

    public Optional<Reservation> update(@NotNull UUID id, @NotNull @Email String newEmail, @NotBlank String newFullname,
                                        @NotNull LocalDate newArrival, @NotNull LocalDate newDeparture) {

        Optional<Reservation> oEntity = repository.findById(id);
        if (oEntity.isEmpty()) {
            // 404
            return oEntity;
        }

        Reservation entity = oEntity.get();
        entity.setEmail(newEmail);
        entity.setFullname(newFullname);

        if (!entity.getArrivalDate().equals(newArrival) || !entity.getDepartureDate().equals(newDeparture)) {
            updateDates(entity, newArrival, newDeparture);
        }

        return Optional.of(saveReservationAndOccupiedDates(entity));
    }

    // This method trx must be attached to parent trx
    // so rollback will rollback parent too
    @Transactional(Transactional.TxType.MANDATORY)
    private Reservation saveReservationAndOccupiedDates(Reservation entity) {
        repository.save(entity);
        List<LocalDate> reservationDates = createDatesBetweenList(entity.getArrivalDate(), entity.getDepartureDate());
        occupiedDateService.saveAll(entity.getId(), reservationDates);
        return entity;
    }

    // This method trx must be attached to parent trx
    // so rollback will rollback parent too
    @Transactional(Transactional.TxType.MANDATORY)
    private void updateDates(Reservation entity, LocalDate newArrival, LocalDate newDeparture ) {
        validateDates(newArrival, newDeparture);

        //TODO first impl (quick and dirty )
        // delete old reservation occupied dates
        // because will fail for existAnyBetweenDates check... but could be same reservation that should be
        occupiedDateService.deleteAllForReservationID(entity.getId());
        //and check if dates are available
        if (occupiedDateService.existAnyBetweenDates(newArrival, newDeparture)) {
            throw new ServiceException("provided dates period is no free, check please check availability for details");
        }

    }


    public void delete(@NotNull UUID id) {
        occupiedDateService.deleteAllForReservationID(id);
        repository.deleteById(id);
    }


    /**
     * Check if dates are valid for reserve the campsite
     * <p>
     * The campsite can be reserved for max 3 days.
     * The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.
     *
     * @param arrivalDate   reservation arrival date
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
            throw new ServiceException("The campsite can be reserved for max 3 days");
        }

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        if (arrivalDate.isBefore(tomorrow)) {
            throw new ServiceException("The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance");
        }

        LocalDate nextMonthDate = today.plusMonths(1);
        if (departureDate.isAfter(nextMonthDate)) {
            throw new ServiceException("The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance");
        }

        LOG.debug("checking reservation dates are valid! [arrivalDate {} - departureDate {}]", arrivalDate, departureDate);
    }

    /**
     * generates a list containing each date
     *
     * @param start
     * @param end
     * @return
     */
    protected List<LocalDate> createDatesBetweenList(LocalDate start, LocalDate end) {
        List<LocalDate> dates = Stream.iterate(start, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(start, end) + 1)
                .collect(Collectors.toList());

        LOG.debug("reservation dates are {}", dates);
        return dates;
    }


}
