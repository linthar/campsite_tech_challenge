package com.upgrade.campsite.exception;

import io.micronaut.http.HttpStatus;

public class ServiceException extends RuntimeException {

    private HttpStatus status;

    public ServiceException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

    public ServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public ServiceException(String message, Throwable cause) {
        this(message, cause, HttpStatus.BAD_REQUEST);
    }

    public ServiceException(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }


    public HttpStatus getStatus() {
        return status;
    }

}
