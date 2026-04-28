package com.mark.minioclient.exception;

public class ManageFileException extends RuntimeException {
    public ManageFileException(String message) {
        super(message);
    }
    public ManageFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
