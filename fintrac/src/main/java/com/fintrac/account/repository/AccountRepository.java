package com.fintrac.account.repository;

import com.fintrac.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserIdAndActiveTrue(Long userId);
    Optional<Account> findByIdAndUserIdAndActiveTrue(Long id, Long userId);
}
