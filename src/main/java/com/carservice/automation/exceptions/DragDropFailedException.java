package com.carservice.automation.exceptions;

public class DragDropFailedException extends RuntimeException {
    public DragDropFailedException(String message) {
        super("Drag and drop operation failed: " + message);
    }

    public DragDropFailedException(String message, Throwable cause) {
        super("Drag and drop operation failed: " + message, cause);
    }
}
