package com.wiresafe.front.exception;

public class AuthenticationRequiredException extends RuntimeException {

    public AuthenticationRequiredException(String s) {
        super(s);
    }

}
