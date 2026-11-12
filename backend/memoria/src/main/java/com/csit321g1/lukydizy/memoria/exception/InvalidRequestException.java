package com.csit321g1.lukydizy.memoria.exception;

/** Thrown when submitted data fails a business rule (returns 400). */
public class InvalidRequestException extends RuntimeException {

    /** Creates an exception with a message explaining what is wrong. */
    public InvalidRequestException(String message) {
        super(message);
    }
}
