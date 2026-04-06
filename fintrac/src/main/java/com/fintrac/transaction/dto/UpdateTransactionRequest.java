package com.fintrac.transaction.dto;

import com.fintrac.transaction.entity.TransactionCategory;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateTransactionRequest {
    @Size(max = 200)
    private String name;
    private TransactionCategory category;
    @Size(max = 500)
    private String note;
}
