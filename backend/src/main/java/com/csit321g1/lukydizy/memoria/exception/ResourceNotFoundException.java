package com.csit321g1.lukydizy.memoria.exception;

/** Thrown when a record does not exist or belongs to another user (returns 404). */
public class ResourceNotFoundException extends RuntimeException {

    /** Creates an exception with a message such as "Reviewer not found." */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
