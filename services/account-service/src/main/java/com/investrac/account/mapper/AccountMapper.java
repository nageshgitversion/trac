package com.investrac.account.mapper;

import com.investrac.account.dto.response.VirtualAccountResponse;
import com.investrac.account.entity.VirtualAccount;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public VirtualAccountResponse toResponse(VirtualAccount a) {
        return VirtualAccountResponse.builder()
            .id(a.getId())
            .userId(a.getUserId())
            .type(a.getType().name())
            .name(a.getName())
            .balance(a.getBalance())
            .interestRate(a.getInterestRate())
            .bankName(a.getBankName())
            .startDate(a.getStartDate())
            .maturityDate(a.getMaturityDate())
            .maturityAmount(a.getMaturityAmount())
            .emiAmount(a.getEmiAmount())
            .emiDay(a.getEmiDay())
            .linkedAccId(a.getLinkedAccId())
            .goalAmount(a.getGoalAmount())
            .goalProgressPercent(a.getGoalProgressPercent())
            .active(a.isActive())
            .emiDueToday(a.isEmiDueToday())
            .maturingSoon(a.isMaturingSoon(7))
            .createdAt(a.getCreatedAt())
            .build();
    }
}
