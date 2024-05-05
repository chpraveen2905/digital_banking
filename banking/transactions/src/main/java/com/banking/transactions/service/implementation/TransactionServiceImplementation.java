package com.banking.transactions.service.implementation;

import com.banking.transactions.exception.AccountStatusException;
import com.banking.transactions.exception.GlobalErrorCode;
import com.banking.transactions.exception.InsufficientBalance;
import com.banking.transactions.exception.ResourceNotFound;
import com.banking.transactions.external.AccountService;
import com.banking.transactions.model.TransactionStatus;
import com.banking.transactions.model.TransactionType;
import com.banking.transactions.model.dto.TransactionDto;
import com.banking.transactions.model.entity.Transaction;
import com.banking.transactions.model.external.Account;
import com.banking.transactions.model.mapper.TransactionMapper;
import com.banking.transactions.model.response.Response;
import com.banking.transactions.model.response.TransactionRequest;
import com.banking.transactions.repository.TransactionRepository;
import com.banking.transactions.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImplementation implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final TransactionMapper transactionMapper = new TransactionMapper();
    @Value("${spring.application.ok}")
    private String ok;

    /**
     * Adds a transaction based on the provided TransactionDto.
     *
     * @param transactionDto the TransactionDto object containing the transaction details
     * @return a Response object indicating the success of the transaction
     * @throws ResourceNotFound       if the requested account is not found on the server
     * @throws AccountStatusException if the account is inactive or closed
     * @throws InsufficientBalance    if there is insufficient balance in the account
     */
    @Override
    public Response addTransaction(TransactionDto transactionDto) {
        ResponseEntity<Account> response = this.accountService.readByAccountNumber(transactionDto.getAccountId());
        if (Objects.isNull(response.getBody())) {
            throw new ResourceNotFound("Requested account not found on the server", GlobalErrorCode.NOT_FOUND);
        }
        Account account = response.getBody();
        Transaction transaction = transactionMapper.convertToEntity(transactionDto);
        if (transactionDto.getTransactionType().equals(TransactionType.DEPOSIT.toString())) {
            account.setAvailableBalance(account.getAvailableBalance().add(transactionDto.getAmount()));
        } else if (transactionDto.getTransactionType().equals(TransactionType.WITHDRAWAL.toString())) {
            if (!account.getAccountStatus().equals("ACTIVE")) {
                log.error("account is either inactive/closed, cannot process the transaction");
                throw new AccountStatusException("account is inactive or closed");
            }
            if (account.getAvailableBalance().compareTo(transactionDto.getAmount()) < 0) {
                log.error("insufficient balance in the account");
                throw new InsufficientBalance("Insufficient balance in the account");
            }
            transaction.setAmount(transactionDto.getAmount().negate());
            account.setAvailableBalance(account.getAvailableBalance().subtract(transactionDto.getAmount()));
        }
        transaction.setReferenceId(UUID.randomUUID().toString());
        transaction.setTransactionType(TransactionType.valueOf(transactionDto.getTransactionType()));
        transaction.setStatus(TransactionStatus.COMPLETED);
        accountService.updateAccount(transactionDto.getAccountId(), account);
        transactionRepository.save(transaction);
        return Response.builder().responseCode(ok).message("Transaction Completed Successfully").build();
    }
    /**
     * Completes the internal transaction by updating the status of each transaction
     * and saving them to the transaction repository.
     *
     * @param transactionDtos the list of transaction DTOs to be processed
     * @return a response indicating the completion of the transaction
     */
    @Override
    public Response internalTransaction(List<TransactionDto> transactionDtos, String transactionReference) {
        // Convert the list of transaction DTOs to entities
        List<Transaction> transactions = transactionMapper.convertToEntityList(transactionDtos);

        // Update the status of each transaction to 'COMPLETED'
        transactions.forEach(transaction->{
            transaction.setTransactionType(TransactionType.INTERNAL_TRANSFER);
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setReferenceId(transactionReference);
        });

        transactionRepository.saveAll(transactions);
        return Response.builder().responseCode(ok).message("Transaction Completed Successfully").build();
    }
    /**
     * Retrieves a list of transaction requests for a given account ID.
     *
     * @param accountId the ID of the account
     * @return a list of transaction requests
     */
    @Override
    public List<TransactionRequest> getTransaction(String accountId) {
        return transactionRepository.findTransactionByAccountId(accountId)
                .stream().map(transaction -> {
                    TransactionRequest transactionRequest = new TransactionRequest();
                    BeanUtils.copyProperties(transaction, transactionRequest);
                    transactionRequest.setTransactionStatus(transaction.getStatus().toString());
                    transactionRequest.setLocalDateTime(transaction.getTransactionDate());
                    transactionRequest.setTransactionType(transaction.getTransactionType().toString());
                    return transactionRequest;
                }).collect(Collectors.toList());
    }
    /**
     * Retrieves a list of TransactionRequests based on a transaction reference.
     *
     * @param transactionReference The reference ID of the transaction
     * @return List of TransactionRequests matching the transaction reference
     */
    @Override
    public List<TransactionRequest> getTransactionByTransactionReference(String transactionReference) {
        return transactionRepository.findTransactionByReferenceId(transactionReference)
                .stream().map(transaction -> {
                    TransactionRequest transactionRequest = new TransactionRequest();
                    BeanUtils.copyProperties(transaction, transactionRequest);
                    transactionRequest.setTransactionStatus(transaction.getStatus().toString());
                    transactionRequest.setLocalDateTime(transaction.getTransactionDate());
                    transactionRequest.setTransactionType(transaction.getTransactionType().toString());
                    return transactionRequest;
                }).collect(Collectors.toList());
    }
}
