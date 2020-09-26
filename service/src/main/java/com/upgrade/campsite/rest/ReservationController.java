package com.upgrade.campsite.rest;

import com.upgrade.campsite.model.Reservation;
import com.upgrade.campsite.dto.ReservationRequest;
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
import com.upgrade.campsite.service.*;

@Controller("/reservation")
@Validated
public class ReservationController {

    private static final Logger LOG = LoggerFactory.getLogger(ReservationController.class);

    @Inject
    private ReservationService service;

    @Post
    public Optional<Reservation> create(@Body @Valid ReservationRequest reservation) {
        return service.create(reservation);
    }


    @Get("/{id}")
    public Optional<Reservation> get(@PathVariable(value = "id") @NotNull UUID id) {
        return service.get(id);

    }

    @Patch("/{id}")
    public Optional<Reservation> update(@PathVariable(value = "id") @NotNull UUID id, @Body @Valid ReservationRequest reservation) {
        return service.update(id, reservation);
    }

    @Delete("/{id}")
    @Status(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable(value = "id") @NotNull UUID id) {
        service.delete(id);
    }

}
