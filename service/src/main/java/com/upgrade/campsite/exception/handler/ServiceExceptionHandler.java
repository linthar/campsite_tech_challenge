package com.upgrade.campsite.exception.handler;

import com.upgrade.campsite.exception.ServiceException;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Produces
@Singleton
@Requires(classes = {ServiceException.class, ExceptionHandler.class})
public class ServiceExceptionHandler implements ExceptionHandler<ServiceException, HttpResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceExceptionHandler.class);

    @Override
    public HttpResponse handle(HttpRequest request, ServiceException exception) {
        StringBuilder msg = new StringBuilder("Handling ServiceException");
        msg.append(exception.getMessage());
        LOG.warn(msg.toString(), exception);

        Map<String, String> response = new HashMap<String, String>();
        response.put("error", exception.getMessage());

        return HttpResponse.status(exception.getStatus()).body(response);
    }

}