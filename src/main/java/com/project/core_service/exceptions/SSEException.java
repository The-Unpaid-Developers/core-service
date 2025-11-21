package com.project.core_service.exceptions;

public class SSEException extends RuntimeException {
    public SSEException(Exception e) {
        super(e);
    }
}
