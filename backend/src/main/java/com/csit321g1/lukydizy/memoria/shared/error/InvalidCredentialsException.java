package com.csit321g1.lukydizy.memoria.shared.error;

public class InvalidCredentialsException extends RuntimeException {

    /** Creates an exception for an invalid email and password combination. */
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
