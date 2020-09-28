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

@MappedEntity("reservation")
@ToString
@EqualsAndHashCode
@Validated
@Introspected
public class Reservation {

    @Id
    private UUID id;

    @Column
    @NotNull
    private String email;

    @Column
    @NotNull
    private String fullname;

    @Column
    @NotNull
    private LocalDate arrivalDate;

    @Column
    @NotNull
    private LocalDate departureDate;

    public Reservation() {
    }

    public Reservation(UUID id, @NotNull String email, @NotNull String fullname, @NotNull LocalDate arrivalDate, @NotNull LocalDate departureDate) {
        this.id = id;
        this.email = email;
        this.fullname = fullname;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }
}
