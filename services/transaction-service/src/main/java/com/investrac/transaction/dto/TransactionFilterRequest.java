package com.investrac.transaction.dto;

import com.investrac.transaction.entity.Transaction;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class TransactionFilterRequest {
    private Transaction.TransactionType type;
    private String category;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;

    private String search;
    private int page = 0;
    private int size = 20;
    private String sortBy = "txDate";
    private String sortDir = "desc";
}
