package com.upgrade.campsite.exception.handler;

import io.micronaut.context.annotation.Requires;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Produces
@Singleton
@Requires(classes = {DataAccessException.class, ExceptionHandler.class})
public class DataAccessExceptionHandler implements ExceptionHandler<DataAccessException, HttpResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(DataAccessExceptionHandler.class);
    public static final String ERROR_MESSAGE = "there is a conflict with one or many dates in you reservation request. Provided dates period are no longer no free, check please check availability for details";

    @Override
    public HttpResponse handle(HttpRequest request, DataAccessException exception) {
        StringBuilder msg = new StringBuilder("Handling DataAccessException");
        msg.append(exception.getMessage());
        LOG.warn(msg.toString(), exception);

        Map<String, String> response = new HashMap<String, String>();
        response.put("error", ERROR_MESSAGE);

        return HttpResponse.status(HttpStatus.CONFLICT).body(response);
    }

}