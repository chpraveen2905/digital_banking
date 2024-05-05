package com.banking.fundtransfer.model.entity;

import com.banking.fundtransfer.model.TransactionStatus;
import com.banking.fundtransfer.model.TransferType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class FundTransfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fundTransferId;

    private String transactionReference;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    private TransferType transferType;

    @CreationTimestamp
    private LocalDateTime transferredOn;
}
