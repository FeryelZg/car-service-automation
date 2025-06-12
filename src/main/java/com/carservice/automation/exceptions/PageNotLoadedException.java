package com.carservice.automation.exceptions;

public class PageNotLoadedException extends RuntimeException {
    public PageNotLoadedException(String pageName) {
        super("Page not loaded properly: " + pageName);
    }

    public PageNotLoadedException(String pageName, Throwable cause) {
        super("Page not loaded properly: " + pageName, cause);
    }
}
