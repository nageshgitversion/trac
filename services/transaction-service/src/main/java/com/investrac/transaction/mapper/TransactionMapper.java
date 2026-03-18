package com.investrac.transaction.mapper;

import com.investrac.transaction.dto.TransactionResponse;
import com.investrac.transaction.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
            .id(t.getId())
            .userId(t.getUserId())
            .walletId(t.getWalletId())
            .accountId(t.getAccountId())
            .type(t.getType())
            .category(t.getCategory())
            .name(t.getName())
            .amount(t.getAmount())
            .envelopeKey(t.getEnvelopeKey())
            .txDate(t.getTxDate())
            .note(t.getNote())
            .source(t.getSource())
            .status(t.getStatus())
            .sagaId(t.getSagaId())
            .failureReason(t.getFailureReason())
            .createdAt(t.getCreatedAt())
            .updatedAt(t.getUpdatedAt())
            .build();
    }
}
