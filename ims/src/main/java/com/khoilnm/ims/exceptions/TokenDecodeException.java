package com.khoilnm.ims.exceptions;

public class TokenDecodeException extends RuntimeException {
    public TokenDecodeException(String message) {
        super(message);
    }
    public TokenDecodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
