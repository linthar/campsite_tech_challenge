package com.upgrade.campsite.rest;

import com.upgrade.campsite.model.Reservation;
import com.upgrade.campsite.dto.CreateReservation;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.validation.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Controller("/reservation")
@Validated
public class ReservationController {

    private static final Logger LOG = LoggerFactory.getLogger(ReservationController.class);

    @Post
    public Optional<Reservation> create(@Body @Valid CreateReservation reservation) {
        return getReservationMock(UUID.randomUUID(), reservation);
    }


    @Get("/{id}")
    public Optional<Reservation> get(@PathVariable(value = "id") @NotNull UUID id) {
        return geMock(id);
    }

    @Patch("/{id}")
    public Optional<Reservation> update(@PathVariable(value = "id") @NotNull UUID id, @Body @Valid CreateReservation reservation) {
        return getReservationMock(id, reservation);
    }

    @Delete("/{id}")
    @Status(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable(value = "id") @NotNull UUID id) {
    }



    private Optional<Reservation> getReservationMock(UUID id, CreateReservation createReservation) {
        Reservation r = new Reservation();
        r.setId(id);
        r.setEmail(createReservation.getEmail());
        r.setFullname(createReservation.getFullname());
        r.setArrivalDate(createReservation.getArrivalDate());
        r.setDepartureDate(createReservation.getDepartureDate());
        return Optional.of(r);
    }
    private Optional<Reservation> geMock(@NotNull UUID id) {
        Reservation r = new Reservation();
        r.setId(id);
        r.setEmail("aa@bb.com");
        r.setFullname("John Smith");
        LocalDate today = LocalDate.now();
        r.setArrivalDate(today.plusDays(1));
        r.setDepartureDate(today.plusDays(2));
        return Optional.of(r);
    }


}
