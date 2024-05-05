package com.banking.userservice.exception;

public class ResourceNotFound extends GlobalException{
    public ResourceNotFound(String message, String errorCode) {
        super("Resource Not Found on the Server", GlobalError.NOT_FOUND);
    }

    public ResourceNotFound(String message) {
        super(message);
    }
}
