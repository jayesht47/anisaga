package com.anisaga.user_service.exceptions;

public class DuplicateUserNameException extends Exception {
    public DuplicateUserNameException(String userName) {
        super("Duplicate UserName found :: " + userName);
    }
}
