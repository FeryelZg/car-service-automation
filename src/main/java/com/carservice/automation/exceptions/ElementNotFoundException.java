package com.carservice.automation.exceptions;

/**
 * Custom exceptions for your car service automation framework
 */
public class ElementNotFoundException extends RuntimeException {
    public ElementNotFoundException(String elementDescription) {
        super("Element not found: " + elementDescription);
    }

    public ElementNotFoundException(String elementDescription, Throwable cause) {
        super("Element not found: " + elementDescription, cause);
    }
}
