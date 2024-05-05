package com.banking.accountService.external;

import com.banking.accountService.model.dto.external.SequenceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "sequence-generator")
public interface SequenceService {

    /**
     * Generates a new account number.
     *
     * @return the generated account number as a SequenceDto object.
     */
    @PostMapping("/sequence")
    SequenceDto generateAccountNumber();
}
