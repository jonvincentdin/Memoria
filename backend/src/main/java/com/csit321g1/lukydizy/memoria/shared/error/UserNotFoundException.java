package com.csit321g1.lukydizy.memoria.shared.error;

public class UserNotFoundException extends RuntimeException {

    /** Creates an exception describing the missing user. */
    public UserNotFoundException(String id) {
        super("User not found with id: " + id);
    }
}
