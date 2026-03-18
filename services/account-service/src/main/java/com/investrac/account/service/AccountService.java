package com.investrac.account.service;

import com.investrac.account.dto.request.CreateAccountRequest;
import com.investrac.account.dto.request.UpdateAccountRequest;
import com.investrac.account.dto.response.AccountSummaryResponse;
import com.investrac.account.dto.response.MaturityCalculationResponse;
import com.investrac.account.dto.response.VirtualAccountResponse;
import com.investrac.account.entity.VirtualAccount;
import com.investrac.account.entity.VirtualAccount.AccountType;
import com.investrac.account.exception.AccountException;
import com.investrac.account.mapper.AccountMapper;
import com.investrac.account.repository.VirtualAccountRepository;
import com.investrac.common.dto.ErrorCodes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {

    private final VirtualAccountRepository accountRepository;
    private final MaturityCalculator       maturityCalculator;
    private final AccountMapper            mapper;

    // ══════════════════════════════════════════
    // CREATE
    // ══════════════════════════════════════════
    @Transactional
    public VirtualAccountResponse createAccount(Long userId, CreateAccountRequest req) {
        log.info("Creating {} account for userId={}", req.getType(), userId);

        VirtualAccount account = VirtualAccount.builder()
            .userId(userId)
            .type(req.getType())
            .name(req.getName().trim())
            .balance(req.getBalance())
            .interestRate(req.getInterestRate() != null ? req.getInterestRate() : BigDecimal.ZERO)
            .bankName(req.getBankName())
            .startDate(req.getStartDate())
            .maturityDate(req.getMaturityDate())
            .emiAmount(req.getEmiAmount() != null ? req.getEmiAmount() : BigDecimal.ZERO)
            .emiDay(req.getEmiDay())
            .linkedAccId(req.getLinkedAccId())
            .goalAmount(req.getGoalAmount())
            .active(true)
            .build();

        // Auto-calculate maturity amount for FD and RD if dates are provided
        if (requiresMaturityCalc(account)) {
            MaturityCalculationResponse calc = maturityCalculator.calculate(
                account.getType(),
                account.getType() == AccountType.RD ? account.getEmiAmount() : account.getBalance(),
                account.getInterestRate(),
                account.getStartDate(),
                account.getMaturityDate()
            );
            account.setMaturityAmount(calc.getMaturityAmount());
            log.debug("Auto-calculated maturity: ₹{} for {} account",
                calc.getMaturityAmount(), account.getType());
        }

        account = accountRepository.save(account);
        log.info("Account created id={} type={} for userId={}", account.getId(), account.getType(), userId);
        return mapper.toResponse(account);
    }

    // ══════════════════════════════════════════
    // GET ALL (with summary)
    // ══════════════════════════════════════════
    @Transactional(readOnly = true)
    public AccountSummaryResponse getAccountSummary(Long userId) {
        List<VirtualAccount> accounts = accountRepository
            .findByUserIdAndActiveTrueOrderByCreatedAtDesc(userId);

        List<Object[]> totals = accountRepository.getSummaryByType(userId);
        Map<String, BigDecimal> typeMap = new HashMap<>();
        totals.forEach(row -> typeMap.put(((AccountType) row[0]).name(), (BigDecimal) row[1]));

        BigDecimal totalEmi = accountRepository.getTotalMonthlyEmi(userId, LocalDate.now());

        List<VirtualAccountResponse> responses = accounts.stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());

        return AccountSummaryResponse.builder()
            .totalSavings(typeMap.getOrDefault("SAVINGS", BigDecimal.ZERO))
            .totalFdCorpus(typeMap.getOrDefault("FD", BigDecimal.ZERO))
            .totalRdCorpus(typeMap.getOrDefault("RD", BigDecimal.ZERO))
            .totalLoanOutstanding(typeMap.getOrDefault("LOAN", BigDecimal.ZERO))
            .totalMonthlyEmi(totalEmi)
            .totalAccounts(accounts.size())
            .accounts(responses)
            .build();
    }

    // ══════════════════════════════════════════
    // GET BY ID
    // ══════════════════════════════════════════
    @Transactional(readOnly = true)
    public VirtualAccountResponse getById(Long id, Long userId) {
        return mapper.toResponse(findByIdAndUser(id, userId));
    }

    // ══════════════════════════════════════════
    // UPDATE
    // ══════════════════════════════════════════
    @Transactional
    public VirtualAccountResponse updateAccount(Long id, Long userId, UpdateAccountRequest req) {
        VirtualAccount account = findByIdAndUser(id, userId);

        if (req.getName()         != null) account.setName(req.getName().trim());
        if (req.getBalance()      != null) account.setBalance(req.getBalance());
        if (req.getInterestRate() != null) account.setInterestRate(req.getInterestRate());
        if (req.getBankName()     != null) account.setBankName(req.getBankName());
        if (req.getMaturityDate() != null) account.setMaturityDate(req.getMaturityDate());
        if (req.getEmiAmount()    != null) account.setEmiAmount(req.getEmiAmount());
        if (req.getEmiDay()       != null) account.setEmiDay(req.getEmiDay());
        if (req.getLinkedAccId()  != null) account.setLinkedAccId(req.getLinkedAccId());
        if (req.getGoalAmount()   != null) account.setGoalAmount(req.getGoalAmount());

        // Recalculate maturity if relevant fields changed
        if (requiresMaturityCalc(account)) {
            MaturityCalculationResponse calc = maturityCalculator.calculate(
                account.getType(),
                account.getType() == AccountType.RD ? account.getEmiAmount() : account.getBalance(),
                account.getInterestRate(),
                account.getStartDate(),
                account.getMaturityDate()
            );
            account.setMaturityAmount(calc.getMaturityAmount());
        }

        account = accountRepository.save(account);
        log.info("Account updated id={} userId={}", id, userId);
        return mapper.toResponse(account);
    }

    // ══════════════════════════════════════════
    // DELETE (soft deactivate)
    // ══════════════════════════════════════════
    @Transactional
    public void deleteAccount(Long id, Long userId) {
        int updated = accountRepository.deactivate(id, userId);
        if (updated == 0) {
            throw new AccountException(ErrorCodes.ACCOUNT_NOT_FOUND,
                "Account not found", HttpStatus.NOT_FOUND);
        }
        log.info("Account deactivated id={} userId={}", id, userId);
    }

    // ══════════════════════════════════════════
    // CALCULATE MATURITY (on-demand endpoint)
    // ══════════════════════════════════════════
    @Transactional(readOnly = true)
    public MaturityCalculationResponse calculateMaturity(Long id, Long userId) {
        VirtualAccount account = findByIdAndUser(id, userId);

        if (account.getType() != AccountType.FD && account.getType() != AccountType.RD) {
            throw new AccountException(ErrorCodes.MATURITY_DATE_INVALID,
                "Maturity calculation is only available for FD and RD accounts",
                HttpStatus.BAD_REQUEST);
        }
        if (account.getStartDate() == null || account.getMaturityDate() == null) {
            throw new AccountException(ErrorCodes.MATURITY_DATE_INVALID,
                "Start and maturity dates are required for calculation",
                HttpStatus.BAD_REQUEST);
        }

        BigDecimal principalOrInstalment = account.getType() == AccountType.RD
            ? account.getEmiAmount()
            : account.getBalance();

        return maturityCalculator.calculate(
            account.getType(),
            principalOrInstalment,
            account.getInterestRate(),
            account.getStartDate(),
            account.getMaturityDate()
        );
    }

    // ══════════════════════════════════════════
    // TOTAL EMI (used by wallet-service)
    // ══════════════════════════════════════════
    @Transactional(readOnly = true)
    public BigDecimal getTotalMonthlyEmi(Long userId) {
        return accountRepository.getTotalMonthlyEmi(userId, LocalDate.now());
    }

    // ── Private helpers ──
    private VirtualAccount findByIdAndUser(Long id, Long userId) {
        return accountRepository.findByIdAndUserId(id, userId)
            .filter(VirtualAccount::isActive)
            .orElseThrow(() -> new AccountException(ErrorCodes.ACCOUNT_NOT_FOUND,
                "Account not found", HttpStatus.NOT_FOUND));
    }

    private boolean requiresMaturityCalc(VirtualAccount account) {
        return (account.getType() == AccountType.FD || account.getType() == AccountType.RD)
            && account.getStartDate() != null
            && account.getMaturityDate() != null
            && account.getInterestRate() != null
            && account.getInterestRate().compareTo(BigDecimal.ZERO) > 0;
    }
}
