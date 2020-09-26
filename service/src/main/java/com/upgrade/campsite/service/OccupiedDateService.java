package com.upgrade.campsite.service;


import com.upgrade.campsite.model.OccupiedDate;
import com.upgrade.campsite.repository.OccupiedDateRepository;
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
import static javax.transaction.Transactional.TxType.REQUIRED;

@Singleton
@Transactional
public class OccupiedDateService {

    private static final Logger LOG = LoggerFactory.getLogger(OccupiedDateService.class);

    @Inject
    private OccupiedDateRepository repository;


    public List<OccupiedDate> findAllBetweenDates(@NotNull LocalDate fromDate, @NotNull LocalDate toDate) {
        return repository.findAllBetweenDates(fromDate, toDate);
    }

    // must be attached to save reservation transaction in order to keep consistent the DB
    @Transactional(MANDATORY)
    public void saveAll(UUID reservationId, List<LocalDate> dates) {
        for (LocalDate d : dates) {
            repository.save(new OccupiedDate(d, reservationId));
        }
    }

    // must be attached to save reservation transaction in order to keep consistent the DB
    @Transactional(MANDATORY)
    public void deleteAllForReservationID(UUID reservationId) {
        repository.deleteByReservationID(reservationId);
    }

//TODO Fix this in tests
    @Transactional(REQUIRED)
    // this method was made to be called from tests clases
    // deleteAllForReservationID requires a Transaction to be attached
    public void deleteAllForReservationIDOpenTransaction(UUID reservationId) {
        this.deleteAllForReservationID(reservationId);
    }
//TODO Fix this in tests
    @Transactional(REQUIRED)
    // this method was made to be called from tests clases
    // saveAll requires a Transaction to be attached
    public void saveAllOpenTransaction(UUID reservationId, List<LocalDate> dates) {
        this.saveAll(reservationId, dates);
    }

}