package com.csit321g1.lukydizy.memoria.exception;

/** Thrown when a record cannot be deleted because other records use it (returns 409). */
public class ResourceInUseException extends RuntimeException {

    /** Creates an exception with a message explaining what still uses the record. */
    public ResourceInUseException(String message) {
        super(message);
    }
}
