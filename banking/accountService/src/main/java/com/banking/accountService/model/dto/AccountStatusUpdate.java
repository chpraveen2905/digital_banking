package com.banking.accountService.model.dto;

import com.banking.accountService.model.AccountStatus;
import lombok.Data;

@Data
public class AccountStatusUpdate {
    AccountStatus accountStatus;
}
