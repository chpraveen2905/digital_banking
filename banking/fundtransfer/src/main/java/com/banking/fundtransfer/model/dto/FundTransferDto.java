package com.banking.fundtransfer.model.dto;

import com.banking.fundtransfer.model.TransactionStatus;
import com.banking.fundtransfer.model.TransferType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FundTransferDto {
    private String transactionReference;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;

    private TransactionStatus status;

    private TransferType transferType;

    private LocalDateTime transferredOn;
}
