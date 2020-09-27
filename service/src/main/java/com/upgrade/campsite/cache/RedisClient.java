package com.upgrade.campsite.cache;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.micronaut.validation.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Singleton
@Validated
public class RedisClient {
    private static final Logger LOG = LoggerFactory.getLogger(RedisClient.class);

    @Inject
    private StatefulRedisConnection<String, String> redisConnection;

    protected RedisCommands<String, String> redisCommands;

    @PostConstruct
    protected void init() {
        redisCommands = redisConnection.sync();
        redisCommands.setTimeout(Duration.ofMillis(500));
    }

    public RedisCommands<String, String> getRedisCommands() {
        return redisCommands;
    }

    public void storeInCache(@NotNull LocalDate key, @NotNull UUID reservationID) {
        this.storeInCache(key, reservationID.toString());
    }

    public void storeInCache(@NotNull LocalDate key, @NotNull String reservationID) {
        LOG.debug("caching key [{}] value: {}", key, reservationID);
        redisCommands.set(key.toString(), reservationID);
    }

    public String getFromCache(@NotNull LocalDate key) {
        return redisCommands.get(key.toString());
    }

    public void removeKey(@NotNull LocalDate key) {
        removeKey(key.toString());
    }

    public void removeKey(@NotNull String key) {
        LOG.debug("deleting key [{}] ", key);
        redisCommands.del(key.toString());
    }

}
