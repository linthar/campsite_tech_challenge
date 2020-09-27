package com.upgrade.campsite.service;


import com.upgrade.campsite.model.OccupiedDate;
import com.upgrade.campsite.repository.OccupiedDateRepository;
import com.upgrade.campsite.service.aop.OccupiedDateCreatedEvent;
import com.upgrade.campsite.service.aop.OccupiedDateDeletedEvent;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.validation.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static javax.transaction.Transactional.TxType.MANDATORY;
import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

@Singleton
@Validated
@Transactional
public class OccupiedDateService {

    private static final Logger LOG = LoggerFactory.getLogger(OccupiedDateService.class);

    @Inject
    private OccupiedDateRepository repository;

    @Inject
    private ApplicationEventPublisher applicationEventPublisher;

    public List<OccupiedDate> findAllBetweenDates(@NotNull LocalDate fromDate, @NotNull LocalDate toDate) {
        return repository.findAllBetweenDates(fromDate, toDate);
    }

    public boolean existAnyBetweenDates(@NotNull LocalDate fromDate, @NotNull LocalDate toDate) {
        return repository.existAnyBetweenDates(fromDate, toDate);
    }

     public boolean isOccupied(@NotNull LocalDate date) {
        return repository.existsById(date);
    }


    // must be attached to save reservation transaction in order to keep consistent the DB
    // so rollback will rollback parent too
    @Transactional(MANDATORY)
    public void saveAll(UUID reservationId, List<LocalDate> dates) {
        LOG.debug("setting dates as occupied: {}", dates);
        for (LocalDate d : dates) {
            // at most there are 3 dates for each reservation
            OccupiedDate od = new OccupiedDate(d, reservationId);
            repository.save(od);
            applicationEventPublisher.publishEvent(new OccupiedDateCreatedEvent(od));
        }
    }

    // must be attached to save reservation transaction in order to keep consistent the DB
    // so rollback will rollback parent too
    @Transactional(MANDATORY)
    public void deleteAllForReservationID(UUID reservationId) {
        LOG.debug("deleting all OccupiedDates for reservation id: {}", reservationId);

        List<OccupiedDate> datesToDelete = repository.findAllByReservationId(reservationId);
        // at most there are 3 dates for each reservation
        for (OccupiedDate od :datesToDelete) {
            applicationEventPublisher.publishEvent(new OccupiedDateDeletedEvent(od));
        }

        // execute DELETE query
        repository.deleteByReservationID(reservationId);

    }


    ////////// methods needed for tests clases

//TODO Fix this in tests
    @Transactional(REQUIRES_NEW)
    // this method was made to be called from tests classes
    // saveAll requires a Transaction to be attached
    public void saveAllOpenTransaction(UUID reservationId, List<LocalDate> dates) {
        this.saveAll(reservationId, dates);
    }

}