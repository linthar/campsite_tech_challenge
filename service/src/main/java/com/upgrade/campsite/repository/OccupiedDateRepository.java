package com.upgrade.campsite.repository;

import com.upgrade.campsite.model.OccupiedDate;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@JdbcRepository(dialect = Dialect.MYSQL)
public interface OccupiedDateRepository extends CrudRepository<OccupiedDate, UUID> {

    @Query(value = "SELECT * FROM occupied_date WHERE date BETWEEN :fromDate and :toDate")
    List<OccupiedDate> findAllBetweenDates(@NotNull LocalDate fromDate, @NotNull LocalDate toDate);

    @Query(value = "DELETE FROM occupied_date WHERE reservation_id = :reservationID")
    void deleteByReservationID(@NotNull UUID reservationID);
}
