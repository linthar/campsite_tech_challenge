package com.upgrade.campsite.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.validation.Validated;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@MappedEntity("occupied_date")
@ToString
@EqualsAndHashCode
@Validated
@Introspected
public class OccupiedDate {

    @Id
    private LocalDate date;

    @Column
    @NotNull
    private UUID reservationId;

    public OccupiedDate() {
    }

    public OccupiedDate(@NotNull LocalDate date, @NotNull UUID reservationId) {
        this.date = date;
        this.reservationId = reservationId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public void setReservationId(UUID reservationId) {
        this.reservationId = reservationId;
    }
}
