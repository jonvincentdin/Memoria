package com.csit321g1.lukydizy.memoria.exception;

public class EmailAlreadyUsedException extends RuntimeException {

    /** Creates an exception for an email that is already registered. */
    public EmailAlreadyUsedException() {
        super("An account with this email already exists.");
    }
}
