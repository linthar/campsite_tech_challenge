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
    private String email;

    public OccupiedDate() {
    }

    public OccupiedDate(LocalDate date, @NotNull String email) {
        this.date = date;
        this.email = email;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
