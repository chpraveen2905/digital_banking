package com.banking.accountService.exception;

public class AccountStatusException extends GlobalException{
    public AccountStatusException(String errorMessage) {
        super(errorMessage, GlobalErrorCode.BAD_REQUEST);
    }
}
