package com.csit321g1.lukydizy.memoria.exception;

public class InvalidCredentialsException extends RuntimeException {

    /** Creates an exception for an invalid email and password combination. */
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
