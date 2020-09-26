package com.upgrade.campsite.repository;

import com.upgrade.campsite.model.Reservation;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.UUID;

@JdbcRepository(dialect = Dialect.MYSQL)
public interface ReservationRepository extends CrudRepository<Reservation, UUID> {


}
