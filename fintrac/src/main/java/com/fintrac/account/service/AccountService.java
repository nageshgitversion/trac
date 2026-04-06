package com.fintrac.account.service;

import com.fintrac.account.dto.*;
import com.fintrac.account.entity.*;
import com.fintrac.account.repository.AccountRepository;
import com.fintrac.common.exception.FinTracException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public AccountResponse create(Long userId, CreateAccountRequest req) {
        LocalDate startDate = req.getStartDate() != null ? req.getStartDate() : LocalDate.now();
        LocalDate maturityDate = (req.getTenureMonths() != null)
            ? startDate.plusMonths(req.getTenureMonths()) : null;

        Account account = Account.builder()
            .userId(userId).type(req.getType()).name(req.getName())
            .principal(req.getPrincipal()).interestRate(req.getInterestRate())
            .tenureMonths(req.getTenureMonths()).startDate(startDate)
            .maturityDate(maturityDate).note(req.getNote()).build();

        return toDto(accountRepository.save(account));
    }

    public List<AccountResponse> listAll(Long userId) {
        return accountRepository.findByUserIdAndActiveTrue(userId)
            .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public AccountResponse update(Long userId, Long id, UpdateAccountRequest req) {
        Account acc = accountRepository.findByIdAndUserIdAndActiveTrue(id, userId)
            .orElseThrow(() -> new FinTracException("FT-4001", "Account not found", HttpStatus.NOT_FOUND));
        if (req.getNote() != null) acc.setNote(req.getNote());
        if (req.getStatus() != null) acc.setStatus(req.getStatus());
        return toDto(accountRepository.save(acc));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Account acc = accountRepository.findByIdAndUserIdAndActiveTrue(id, userId)
            .orElseThrow(() -> new FinTracException("FT-4001", "Account not found", HttpStatus.NOT_FOUND));
        acc.setActive(false);
        accountRepository.save(acc);
    }

    public AccountProjectionResponse projection(Long userId, Long id) {
        Account acc = accountRepository.findByIdAndUserIdAndActiveTrue(id, userId)
            .orElseThrow(() -> new FinTracException("FT-4001", "Account not found", HttpStatus.NOT_FOUND));

        if (acc.getInterestRate() == null || acc.getTenureMonths() == null) {
            return AccountProjectionResponse.builder()
                .maturityAmount(acc.getPrincipal()).totalInterest(BigDecimal.ZERO).build();
        }

        BigDecimal p = acc.getPrincipal();
        BigDecimal r = acc.getInterestRate().divide(BigDecimal.valueOf(100 * 12), 10, RoundingMode.HALF_UP);
        int n = acc.getTenureMonths();
        BigDecimal maturityAmount;
        BigDecimal monthlyEmi = null;

        if (acc.getType() == AccountType.LOAN) {
            // EMI = P * r * (1+r)^n / ((1+r)^n - 1)
            BigDecimal onePlusR = BigDecimal.ONE.add(r);
            BigDecimal pow = onePlusR.pow(n, new MathContext(10));
            monthlyEmi = p.multiply(r).multiply(pow)
                .divide(pow.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
            maturityAmount = monthlyEmi.multiply(BigDecimal.valueOf(n));
        } else {
            // Compound interest (quarterly for FD/RD, simple for SIP)
            BigDecimal rAnnual = acc.getInterestRate().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
            maturityAmount = p.multiply(
                BigDecimal.ONE.add(rAnnual.divide(BigDecimal.valueOf(4), 10, RoundingMode.HALF_UP))
                .pow((int)(n / 3.0), new MathContext(10))
            ).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal totalInterest = maturityAmount.subtract(p).setScale(2, RoundingMode.HALF_UP);

        return AccountProjectionResponse.builder()
            .maturityAmount(maturityAmount).totalInterest(totalInterest)
            .monthlyEmi(monthlyEmi).build();
    }

    private AccountResponse toDto(Account a) {
        return AccountResponse.builder()
            .id(a.getId()).userId(a.getUserId()).type(a.getType()).name(a.getName())
            .principal(a.getPrincipal()).interestRate(a.getInterestRate())
            .tenureMonths(a.getTenureMonths()).startDate(a.getStartDate())
            .maturityDate(a.getMaturityDate()).status(a.getStatus())
            .note(a.getNote()).createdAt(a.getCreatedAt()).build();
    }
}
