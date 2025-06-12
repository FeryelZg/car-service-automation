package com.carservice.automation.exceptions;

public class WorkspaceSelectionException extends RuntimeException {
    public WorkspaceSelectionException(String workspace) {
        super("Failed to select workspace: " + workspace);
    }

    public WorkspaceSelectionException(String workspace, Throwable cause) {
        super("Failed to select workspace: " + workspace, cause);
    }
}