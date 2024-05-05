package com.banking.fundtransfer.controller;

import com.banking.fundtransfer.model.dto.FundTransferDto;
import com.banking.fundtransfer.model.dto.request.FundTransferRequest;
import com.banking.fundtransfer.service.FundTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.banking.fundtransfer.model.dto.response.FundTransferResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/fund-transfers")
public class FundTransferController {
    private final FundTransferService fundTransferService;

    /**
     * Handles the fund transfer request.
     *
     * @param fundTransferRequest The fund transfer request object.
     * @return The response entity containing the fund transfer response.
     */
    @PostMapping
    public ResponseEntity<FundTransferResponse> fundTransfer(@RequestBody FundTransferRequest fundTransferRequest) {
        return new ResponseEntity<>(fundTransferService.fundTransfer(fundTransferRequest), HttpStatus.CREATED);
    }


    /**
     * Retrieves the transfer details from the given reference ID.
     *
     * @param referenceId the reference ID of the transfer
     * @return the transfer details as a ResponseEntity
     */

    @GetMapping("/{referenceId}")
    public ResponseEntity<FundTransferDto> getTransferDetailsFromReferenceId(@PathVariable String referenceId) {
        return new ResponseEntity<>(fundTransferService.getTransferDetailsFromReferenceId(referenceId), HttpStatus.OK);
    }

    /**
     * Retrieves all fund transfers by account ID.
     *
     * @param accountId the ID of the account
     * @return the list of fund transfer DTOs
     */

    @GetMapping
    public ResponseEntity<List<FundTransferDto>> getAllTransfersByAccountId(@RequestParam String accountId) {
        return new ResponseEntity<>(fundTransferService.getAllFundTransferByAccountId(accountId), HttpStatus.OK);
    }
}
