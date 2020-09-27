package com.upgrade.campsite.rest.dto;

import io.micronaut.core.annotation.Introspected;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@ToString
@EqualsAndHashCode
@Introspected
public class ReservationRequest {

    @NotNull
    @Email
    private String email;

    @NotBlank
    private String fullname;

    @NotNull
    private LocalDate arrivalDate;

    @NotNull
    private LocalDate departureDate;

    public ReservationRequest() {
    }

    public ReservationRequest(@NotNull @Email String email, @NotBlank String fullname, @NotNull LocalDate arrivalDate, @NotNull LocalDate departureDate) {
        this.email = email;
        this.fullname = fullname;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
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
