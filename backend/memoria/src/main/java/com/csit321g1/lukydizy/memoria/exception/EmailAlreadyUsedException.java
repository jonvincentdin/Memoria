package com.csit321g1.lukydizy.memoria.exception;

public class EmailAlreadyUsedException extends RuntimeException {

    public EmailAlreadyUsedException() {
        super("An account with this email already exists.");
    }
}
