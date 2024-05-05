package com.banking.accountService.service.implementation;

import com.banking.accountService.exception.*;
import com.banking.accountService.external.SequenceService;
import com.banking.accountService.external.TransactionService;
import com.banking.accountService.external.UserService;
import com.banking.accountService.model.AccountStatus;
import com.banking.accountService.model.AccountType;
import com.banking.accountService.model.dto.AccountDto;
import com.banking.accountService.model.dto.AccountStatusUpdate;
import com.banking.accountService.model.dto.external.TransactionResponse;
import com.banking.accountService.model.dto.external.UserDto;
import com.banking.accountService.model.dto.response.Response;
import com.banking.accountService.model.entity.Account;
import com.banking.accountService.model.mapper.AccountMapper;
import com.banking.accountService.repository.AccountRepository;
import com.banking.accountService.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static com.banking.accountService.model.Constants.ACC_PREFIX;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImplementation implements AccountService {
    private final UserService userService;
    private final SequenceService sequenceService;
    private final TransactionService transactionService;

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper = new AccountMapper();

    @Value("${spring.application.ok}")
    private String success;

    /**
     * Creates an account based on the provided accountDto.
     *
     * @param accountDto The accountDto containing the necessary information to create an account.
     * @return The response indicating the result of the account creation.
     * @throws ResourceNotFound If the user associated with the accountDto does not exist.
     * @throws ResourceConflict If an account with the same userId and accountType already exists.
     */
    @Override
    public Response createAccount(AccountDto accountDto) {
        ResponseEntity<UserDto> user = this.userService.readUserById(accountDto.getUserId());
        if (Objects.isNull(user.getBody())) {
            throw new ResourceNotFound("user not found on the server");
        }
        this.accountRepository.findAccountByUserIdAndAccountType(accountDto.getUserId(),
                AccountType.valueOf(accountDto.getAccountType())).ifPresent(
                account -> {
                    log.error("Account Already Exists on Server");
                    throw new ResourceConflict("Account Already Exists on Server");
                }
        );
        Account account = accountMapper.convertToEntity(accountDto);
        account.setAccountNumber(ACC_PREFIX + String.format("%07d",
                sequenceService.generateAccountNumber().getAccountNumber()));
        account.setAccountStatus(AccountStatus.PENDING);
        account.setAccountType(AccountType.valueOf(accountDto.getAccountType()));
        account.setAvailableBalance(BigDecimal.valueOf(0));
        account.setUserId(user.getBody().getUserId());
        accountRepository.save(account);
        return Response.builder().responseCode(success).message("Account Created Successfully").build();
    }

    @Override
    public Response updateStatus(String accountNumber, AccountStatusUpdate accountStatusUpdate) {

        return accountRepository.findAccountByAccountNumber(accountNumber).map(
                account -> {
                    if (account.getAccountStatus().equals(AccountStatus.ACTIVE)) {
                        throw new AccountStatusException("Account is Inactive/ closed");
                    }
                    if (account.getAvailableBalance().compareTo(BigDecimal.ZERO) < 0
                            || account.getAvailableBalance().compareTo(BigDecimal.valueOf(1000)) < 0) {
                        throw new InSufficientFunds("Minimum Balance of Rs.1000 is Required");
                    }
                    account.setAccountStatus(accountStatusUpdate.getAccountStatus());
                    accountRepository.save(account);
                    return Response.builder().message("Account Updated Successfully").responseCode(success).build();
                }
        ).orElseThrow(() -> new ResourceNotFound("Account Not on the Server"));
    }

    @Override
    public AccountDto readAccountByAccountNumber(String accountNumber) {
        return accountRepository.findAccountByAccountNumber(accountNumber)
                .map(
                        account -> {
                            AccountDto accountDto = accountMapper.convertToDto(account);
                            accountDto.setAccountType(account.getAccountType().toString());
                            accountDto.setAccountStatus(account.getAccountStatus().toString());
                            return accountDto;
                        }
                ).orElseThrow(ResourceNotFound::new);
    }

    /**
     * Updates an account with the provided account number and account DTO.
     *
     * @param accountNumber The account number of the account to be updated.
     * @param accountDto    The account DTO containing the updated account information.
     * @return A response indicating the success or failure of the account update.
     * @throws AccountStatusException If the account is inactive or closed.
     * @throws ResourceNotFound       If the account is not found on the server.
     */
    @Override
    public Response updateAccount(String accountNumber, AccountDto accountDto) {
        return accountRepository.findAccountByAccountNumber(accountNumber).map(
                account -> {
                    BeanUtils.copyProperties(accountDto, account);
                    accountRepository.save(account);
                    return Response.builder().responseCode(success).message("Account Updated Successfully").build();
                }
        ).orElseThrow(
                () -> new ResourceNotFound("Account Not Found on the Server")
        );
    }

    /**
     * Retrieves the balance for a given account number.
     *
     * @param accountNumber The account number to retrieve the balance for.
     * @return The balance of the account as a string.
     * @throws ResourceNotFound if the account with the given account number is not found.
     */
    @Override

    public String getBalance(String accountNumber) {
        return accountRepository.findAccountByAccountNumber(accountNumber)
                .map(account -> account.getAvailableBalance().toString()
                )
                .orElseThrow(ResourceNotFound::new);

    }

    /**
     * Retrieves a list of transaction responses from the given account ID.
     *
     * @param accountId The ID of the account to retrieve transactions from
     * @return A list of transaction responses
     */
    @Override
    public List<TransactionResponse> getTransactionsFromAccountId(String accountId) {
        return transactionService.getTransactionsFromAccountId(accountId);
    }

    /**
     * Closes the account with the specified account number.
     *
     * @param accountNumber The account number of the account to be closed.
     * @return A response indicating the result of the operation.
     * @throws ResourceNotFound        If the account with the specified account number is not found.
     * @throws AccountClosingException If the balance of the account is not zero.
     */
    @Override
    public Response closeAccount(String accountNumber) {
        return accountRepository.findAccountByAccountNumber(accountNumber)
                .map(account -> {
                    if (BigDecimal.valueOf(Double.parseDouble(getBalance(accountNumber))).compareTo(BigDecimal.ZERO) != 0) {
                        throw new AccountClosingException("Balance Should be Zero");
                    }
                    account.setAccountStatus(AccountStatus.CLOSED);
                    return Response.builder().message("Account Closed Successfully").message(success).build();
                })
                .orElseThrow(ResourceNotFound::new);
    }

    /**
     * Read the account details for a given user ID.
     *
     * @param userId the ID of the user
     * @return the account details as an AccountDto object
     * @throws ResourceNotFound       if no account is found for the user
     * @throws AccountStatusException if the account is inactive or closed
     */
    @Override
    public AccountDto readAccountByUserId(Long userId) {
        return accountRepository.findAccountByUserId(userId)
                .map(
                        account -> {
                            if (!account.getAccountStatus().equals(AccountStatus.ACTIVE)) {
                                throw new AccountStatusException("Account is Inactive or Closed");
                            }
                            AccountDto accountDto = accountMapper.convertToDto(account);
                            accountDto.setAccountStatus(account.getAccountStatus().toString());
                            accountDto.setAccountType(account.getAccountType().toString());
                            return accountDto;
                        }
                )
                .orElseThrow(ResourceNotFound::new);
    }
}
