package com.csit321g1.lukydizy.memoria.shared.error;

public class InvalidRegistrationException extends RuntimeException {

    /** Creates an exception for invalid registration data. */
    public InvalidRegistrationException(String message) {
        super(message);
    }
}
