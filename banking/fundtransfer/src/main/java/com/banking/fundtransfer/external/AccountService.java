package com.banking.fundtransfer.external;

import com.banking.fundtransfer.configuration.FeignClientConfiguration;
import com.banking.fundtransfer.model.dto.external.Account;
import com.banking.fundtransfer.model.dto.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "account-service", configuration = FeignClientConfiguration.class)
public interface AccountService {
    /**
     * Retrieves an account by account number.
     *
     * @param accountNumber The account number to search for.
     * @return The account matching the account number.
     */
    @GetMapping("/accounts")
    ResponseEntity<Account> readByAccountNumber(@RequestParam String accountNumber);
    /**
     * Updates an account with the given account number.
     *
     * @param accountNumber The account number of the account to be updated.
     * @param account The updated account details.
     * @return The response entity containing the response.
     */

    @PutMapping("/accounts")
    ResponseEntity<Response> updateAccount(@RequestParam String accountNumber, @RequestBody Account account);
}
