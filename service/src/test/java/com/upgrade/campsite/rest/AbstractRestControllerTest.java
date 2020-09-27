package com.upgrade.campsite.rest;

import com.upgrade.campsite.cache.RedisClient;
import com.upgrade.campsite.repository.OccupiedDateRepository;
import com.upgrade.campsite.repository.ReservationRepository;
import com.upgrade.campsite.service.AvailabilityService;
import com.upgrade.campsite.service.OccupiedDateService;
import com.upgrade.campsite.service.ReservationService;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import javax.inject.Inject;

public abstract class AbstractRestControllerTest {


    @Inject
    @Client("/")
    protected RxHttpClient client;

    @Inject
    protected OccupiedDateService occupiedDateService;

    @Inject
    protected ReservationService service;

    @Inject
    protected OccupiedDateRepository occupiedDateRepository;

    @Inject
    protected AvailabilityService availabilityService;

    @Inject
    protected ReservationRepository reservationRepository;


    @Inject
    protected RedisClient redisClient;

    void setUp() {
    }

    void tearDown() {
        // clean up all keys from embedded REDIS
        redisClient.getRedisCommands().flushall();

        // starts with an empty DB to avoid false negative in tests
        // (reservation dates are random)
        occupiedDateRepository.deleteAll();
        reservationRepository.deleteAll();
    }
}