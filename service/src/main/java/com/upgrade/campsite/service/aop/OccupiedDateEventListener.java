package com.upgrade.campsite.service.aop;

import com.upgrade.campsite.cache.RedisClient;
import com.upgrade.campsite.model.OccupiedDate;
import io.micronaut.transaction.annotation.TransactionalEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.micronaut.transaction.annotation.TransactionalEventListener.TransactionPhase;

@Singleton
public class OccupiedDateEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(OccupiedDateEventListener.class);

    @Inject
    private RedisClient redisClient;

    @TransactionalEventListener(TransactionPhase.AFTER_COMMIT)
    void created(OccupiedDateCreatedEvent event) {
        OccupiedDate od = event.getOccupiedDate();
        LOG.debug("Listening OccupiedDateCreatedEvent for date: {}", od.getDate());
        redisClient.storeInCache(od.getDate(), od.getReservationId());
    }

    @TransactionalEventListener(TransactionPhase.AFTER_COMMIT)
    void deleted(OccupiedDateDeletedEvent event) {
        OccupiedDate od = event.getOccupiedDate();
        LOG.debug("Listening OccupiedDateDeletedEvent for date: {}", od.getDate());
        redisClient.removeKey(od.getDate());
    }


}
