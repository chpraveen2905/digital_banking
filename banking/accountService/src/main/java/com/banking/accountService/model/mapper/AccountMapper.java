package com.banking.accountService.model.mapper;

import com.banking.accountService.model.dto.AccountDto;
import com.banking.accountService.model.entity.Account;
import org.springframework.beans.BeanUtils;

import java.util.Objects;

public class AccountMapper extends BaseMapper<Account, AccountDto>{
    @Override
    public Account convertToEntity(AccountDto dto, Object... args) {
        Account account = new Account();
        if(!Objects.isNull(dto)){
            BeanUtils.copyProperties(dto, account);
        }
        return account;
    }

    @Override
    public AccountDto convertToDto(Account entity, Object... args) {
        AccountDto accountDto = new AccountDto();
        if(!Objects.isNull(entity)) {
            BeanUtils.copyProperties(entity, accountDto);
        }
        return accountDto;
    }
}
