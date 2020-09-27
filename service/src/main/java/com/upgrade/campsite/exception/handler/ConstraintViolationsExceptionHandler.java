package com.upgrade.campsite.exception.handler;

import com.upgrade.campsite.exception.ConstraintViolationError;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.*;

@Produces
@Singleton
@Replaces(io.micronaut.validation.exceptions.ConstraintExceptionHandler.class)
@Requires(classes = {ConstraintViolationException.class, ExceptionHandler.class})
public class ConstraintViolationsExceptionHandler implements ExceptionHandler<ConstraintViolationException, HttpResponse<ConstraintViolationError>> {
    private static final Logger LOG = LoggerFactory.getLogger(ConstraintViolationsExceptionHandler.class);

    @Override
    public HttpResponse<ConstraintViolationError> handle(HttpRequest request, ConstraintViolationException exception) {
        LOG.error(getLogMessage(exception), exception);
        return HttpResponse.badRequest(new ConstraintViolationError(
                getMessages(exception)
        ));
    }

    private Map<String, List<String>> getMessages(ConstraintViolationException exception) {
        Map<String, List<String>> result = new HashMap<>();

        exception.getConstraintViolations().forEach(constraintViolation -> {
            String jsonPath = getJSONPath(constraintViolation.getPropertyPath());
            result.put(jsonPath, Collections.singletonList(constraintViolation.getMessage()));
        });
        return result;
    }

    private String getJSONPath(Path path) {
        List<String> finalPath = new ArrayList<>();
        path.forEach(node -> {
            if (node instanceof Path.PropertyNode && !(node instanceof Path.ParameterNode)) {
                finalPath.add(node.getName());
            }
        });
        return String.join(".", finalPath);
    }

    private String getLogMessage(ConstraintViolationException exception) {
        StringBuilder msg = new StringBuilder("Handling ConstraintViolationException: ");
        msg.append(exception.getMessage());
        return msg.toString();
    }
}
