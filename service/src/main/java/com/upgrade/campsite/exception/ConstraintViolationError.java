package com.upgrade.campsite.exception;

import java.util.List;
import java.util.Map;

public class ConstraintViolationError {
    private String message;

    private String code;

    private Map<String, List<String>> details;

    public ConstraintViolationError(Map<String, List<String>> errorDetails) {
        this("100001", "Request Body is invalid", errorDetails);
    }

    public ConstraintViolationError(String code, String message, Map<String, List<String>> errorDetails) {
        this.code = code;
        this.message = message;
        this.details = errorDetails;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, List<String>> getDetails() {
        return details;
    }

    public void setDetails(Map<String, List<String>> details) {
        this.details = details;
    }
}
