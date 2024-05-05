package com.banking.fundtransfer.exception;

public class AccountUpdateException extends GlobalException{
    public AccountUpdateException(String errorCode, String message) {
        super(errorCode, message);
    }
}
