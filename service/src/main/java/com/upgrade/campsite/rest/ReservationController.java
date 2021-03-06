package com.upgrade.campsite.rest;

import com.upgrade.campsite.model.Reservation;
import com.upgrade.campsite.rest.dto.ReservationRequest;
import com.upgrade.campsite.service.ReservationService;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.validation.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.UUID;

@Controller("/reservation")
@Validated
public class ReservationController {

    private static final Logger LOG = LoggerFactory.getLogger(ReservationController.class);

    @Inject
    private ReservationService service;

    @Post
    public Reservation create(@Body @Valid ReservationRequest reservationRequest) {
        return service.create(reservationRequest.getEmail(), reservationRequest.getFullname(),
                reservationRequest.getArrivalDate(), reservationRequest.getDepartureDate());
    }

    @Get("/{id}")
    public Optional<Reservation> get(@PathVariable(value = "id") @NotNull UUID id) {
        return service.findByID(id);

    }

    @Patch("/{id}")
    public Optional<Reservation> update(@PathVariable(value = "id") @NotNull UUID id, @Body @Valid ReservationRequest reservation) {
        return service.update(id, reservation.getEmail(), reservation.getFullname(), reservation.getArrivalDate(), reservation.getDepartureDate());
    }

    @Delete("/{id}")
    @Status(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable(value = "id") @NotNull UUID id) {
        service.delete(id);
    }

}
