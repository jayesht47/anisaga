package com.anisaga.anisaga_service.exceptions;


/**
 * Generic exception to use when having to respond to a bad request.
 */
public class BadRequestException extends Exception {
    public BadRequestException(String message) {
        super(message);
    }
}
