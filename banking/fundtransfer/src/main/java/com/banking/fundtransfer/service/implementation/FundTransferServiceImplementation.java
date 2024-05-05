package com.banking.fundtransfer.service.implementation;

import com.banking.fundtransfer.exception.AccountUpdateException;
import com.banking.fundtransfer.exception.GlobalErrorCode;
import com.banking.fundtransfer.exception.InsufficientBalance;
import com.banking.fundtransfer.exception.ResourceNotFound;
import com.banking.fundtransfer.external.AccountService;
import com.banking.fundtransfer.external.TransactionService;
import com.banking.fundtransfer.model.TransactionStatus;
import com.banking.fundtransfer.model.TransferType;
import com.banking.fundtransfer.model.dto.FundTransferDto;
import com.banking.fundtransfer.model.dto.external.Account;
import com.banking.fundtransfer.model.dto.external.Transaction;
import com.banking.fundtransfer.model.dto.request.FundTransferRequest;
import com.banking.fundtransfer.model.dto.response.FundTransferResponse;
import com.banking.fundtransfer.model.entity.FundTransfer;
import com.banking.fundtransfer.model.mapper.FundTransferMapper;
import com.banking.fundtransfer.repository.FundTransferRepository;
import com.banking.fundtransfer.service.FundTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FundTransferServiceImplementation implements FundTransferService {

    private final FundTransferRepository fundTransferRepository;
    private final AccountService accountService;
    private final TransactionService transactionService;
    @Value("${spring.application.ok}")
    private String ok;
    private final FundTransferMapper fundTransferMapper = new FundTransferMapper();

    /**
     * Transfers funds from one account to another.
     *
     * @param fundTransferRequest The request object containing the details of the fund transfer.
     * @return The response object indicating the status of the fund transfer.
     * @throws ResourceNotFound       If the requested account is not found on the server.
     * @throws AccountUpdateException If the account status is pending or inactive.
     * @throws InsufficientBalance    If the required amount to transfer is not available.
     */

    @Override
    public FundTransferResponse fundTransfer(FundTransferRequest fundTransferRequest) {
        Account fromAccount;
        ResponseEntity<Account> response = this.accountService.readByAccountNumber(fundTransferRequest.getFromAccount());
        if (Objects.isNull(response.getBody())) {
            log.error("Requested Account " + fundTransferRequest.getFromAccount() + " is not found on the Server");
            throw new ResourceNotFound("Request Account Not found on the server ", GlobalErrorCode.NOT_FOUND);
        }
        fromAccount = response.getBody();
        if (!fromAccount.getAccountStatus().equals("ACTIVE")) {
            log.error("account status is pending or inactive, please update the account status");
            throw new AccountUpdateException("account is status is :pending", GlobalErrorCode.NOT_ACCEPTABLE);
        }
        if (fromAccount.getAvailableBalance().compareTo(fundTransferRequest.getAmount()) < 0) {
            log.error("required amount to transfer is not available");
            throw new InsufficientBalance("requested amount is not available", GlobalErrorCode.NOT_ACCEPTABLE);
        }
        Account toAccount;
        response = this.accountService.readByAccountNumber(fundTransferRequest.getToAccount());
        if (Objects.isNull(response.getBody())) {
            log.error("Requested Account " + fundTransferRequest.getToAccount() + " is not found on the Server");
            throw new ResourceNotFound("Request Account Not found on the server ", GlobalErrorCode.NOT_FOUND);
        }
        toAccount = response.getBody();
        String transactionReferenceId = internalTransfer(fromAccount, toAccount, fundTransferRequest.getAmount());

        FundTransfer fundTransfer = FundTransfer.builder()
                .transactionReference(transactionReferenceId)
                .fromAccount(fundTransferRequest.getFromAccount())
                .toAccount(fundTransferRequest.getToAccount())
                .amount(fundTransferRequest.getAmount())
                .status(TransactionStatus.SUCCESS)
                .transferType(TransferType.INTERNAL).build();

        fundTransferRepository.save(fundTransfer);
        return FundTransferResponse
                .builder()
                .transactionId(transactionReferenceId)
                .message("Fund Transfer Success")
                .build();
    }

    private String internalTransfer(Account fromAccount, Account toAccount, BigDecimal amount) {
        fromAccount.setAvailableBalance(fromAccount.getAvailableBalance().subtract(amount));
        accountService.updateAccount(fromAccount.getAccountNumber(), fromAccount);
        toAccount.setAvailableBalance(toAccount.getAvailableBalance().add(amount));
        accountService.updateAccount(toAccount.getAccountNumber(), toAccount);

        List<Transaction> transactions = List.of(
                Transaction.builder()
                        .accountId(fromAccount.getAccountNumber())
                        .transactionType("INTERNAL_TRANSFER")
                        .amount(amount.negate())
                        .description("Internal Fund Transfer from " +
                                fromAccount.getAccountNumber() + " to " + toAccount.getAccountNumber()).build(),
                Transaction.builder()
                        .accountId(toAccount.getAccountNumber())
                        .transactionType("INTERNAL_TRANSFER")
                        .amount(amount)
                        .description("Internal fund transfer received from: " +
                                fromAccount.getAccountNumber()).build()
        );
        String transactionReference = UUID.randomUUID().toString();
        transactionService.makeInternalTransactions(transactions, transactionReference);
        return transactionReference;
    }

    @Override
    public FundTransferDto getTransferDetailsFromReferenceId(String referenceId) {
        return fundTransferRepository.findFundTransferByTransactionReference(referenceId)
                .map(fundTransferMapper::convertToDto)
                .orElseThrow(() -> new ResourceNotFound("Fund Transfer Not Found", GlobalErrorCode.NOT_FOUND));
    }

    /**
     * Retrieves a list of fund transfers associated with the given account ID.
     *
     * @param accountId The ID of the account
     * @return A list of fund transfer DTOs
     */
    @Override
    public List<FundTransferDto> getAllFundTransferByAccountId(String accountId) {
        return fundTransferMapper.convertToDtoList(fundTransferRepository.findFundTransferByFromAccount(accountId));
    }


}
