package com.banking.transactions.model.dto;

import com.banking.transactions.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionDto {
    private String accountId;
    private String transactionType;
    private BigDecimal amount;
    private String description;
}
