package com.upgrade.campsite.service;

import com.upgrade.campsite.exception.ServiceException;
import com.upgrade.campsite.model.Reservation;
import com.upgrade.campsite.repository.ReservationRepository;
import com.upgrade.campsite.utils.DatesValidator;
import io.micronaut.validation.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
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
@Validated
public class ReservationService {
    private static final Logger LOG = LoggerFactory.getLogger(ReservationService.class);

    @Inject
    private OccupiedDateService occupiedDateService;

    @Inject
    private ReservationRepository repository;

    @Inject
    private AvailabilityService availabilityService;

    @Inject
    private DatesValidator datesValidator;


    // this method must handle the "parent" transaction
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Reservation create(@NotNull @Email String email, @NotBlank String fullname,
                              @NotNull LocalDate arrival, @NotNull LocalDate departure) {

        datesValidator.validateReservationDates(arrival, departure);
        LOG.debug("creating reservation for dates [arrival: {} - departure: {}]", arrival, departure);
        //and check if dates are available
        checkVacanciesForDates(arrival, departure);

        LOG.debug("dates are vacant [arrival: {} - departure: {}]", arrival, departure);

        Reservation entity = new Reservation();
        UUID reservationId = UUID.randomUUID();
        entity.setId(reservationId);
        entity.setEmail(email);
        entity.setFullname(fullname);
        entity.setArrivalDate(arrival);
        entity.setDepartureDate(departure);
        repository.save(entity);

        saveOccupiedDates(entity);
        return entity;
    }

    public Optional<Reservation> findByID(@NotNull UUID id) {
        LOG.debug("finding reservation by id: {}", id);
        return repository.findById(id);
    }

    // this method must handle the "parent" transaction
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void delete(@NotNull UUID id) {
        occupiedDateService.deleteAllForReservationID(id);
        repository.deleteById(id);
    }

    // this method must handle the "parent" transaction
    @Transactional(Transactional.TxType.REQUIRES_NEW)
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
        repository.update(entity);
        saveOccupiedDates(entity);
        return Optional.of(entity);
    }

    // This method trx must be attached to parent trx
    // so rollback will rollback parent too
    @Transactional(Transactional.TxType.MANDATORY)
    private void saveOccupiedDates(Reservation entity) {
        List<LocalDate> reservationDates = createDatesBetweenList(entity.getArrivalDate(), entity.getDepartureDate());
        occupiedDateService.saveAll(entity.getId(), reservationDates);
    }

    // This method trx must be attached to parent trx
    // so rollback will rollback parent too
    @Transactional(Transactional.TxType.MANDATORY)
    private void updateDates(Reservation entity, LocalDate newArrival, LocalDate newDeparture ) {
        datesValidator.validateReservationDates(newArrival, newDeparture);

        //TODO first impl (quick and dirty )
        // delete old reservation occupied dates
        // because will fail for existAnyBetweenDates check... but could be same reservation that should be
        occupiedDateService.deleteAllForReservationID(entity.getId());
        //and check if dates are available
        checkVacanciesForDates(newArrival, newDeparture);
        entity.setArrivalDate(newArrival);
        entity.setDepartureDate(newDeparture);
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

    /**
     * verifies that all dates between the given dates (inclusive) are FREE (NOT OCCUPIED)
     * @param newArrival first date to check
     * @param newDeparture last date to check
     */
    protected void checkVacanciesForDates(LocalDate newArrival, LocalDate newDeparture) {
        if (!availabilityService.isAvailableBetweenDates(newArrival, newDeparture)) {
            throw new ServiceException("provided dates period is no free, check please check availability for details");
        }
    }

}
