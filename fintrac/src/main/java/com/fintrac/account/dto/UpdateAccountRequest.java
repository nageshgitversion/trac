package com.fintrac.account.dto;

import com.fintrac.account.entity.AccountStatus;
import lombok.Data;

@Data
public class UpdateAccountRequest {
    private String note;
    private AccountStatus status;
}
