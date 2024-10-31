package com.anisaga.user_service.exceptions;

public class RegistrationValidationException extends Exception {
    public RegistrationValidationException(String reason) {
        super("Registration validation failed with reason" + reason);
    }
}
