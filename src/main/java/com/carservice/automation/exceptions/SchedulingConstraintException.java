package com.carservice.automation.exceptions;

public class SchedulingConstraintException extends RuntimeException {
    public SchedulingConstraintException(String constraint, String details) {
        super("Scheduling constraint violated: " + constraint + " - " + details);
    }

    public SchedulingConstraintException(String constraint, String details, Throwable cause) {
        super("Scheduling constraint violated: " + constraint + " - " + details, cause);
    }
}
