package com.upgrade.campsite.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.transaction.Transactional;

@Singleton
@Transactional
public class ReservationService {
    private static final Logger LOG = LoggerFactory.getLogger(ReservationService.class);


    //TODO  descomentar el    @Transactional(MANDATORY) del  saveAll

}
