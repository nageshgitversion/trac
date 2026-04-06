package com.fintrac.portfolio.repository;

import com.fintrac.portfolio.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
    List<Holding> findByUserIdAndActiveTrue(Long userId);
    Optional<Holding> findByIdAndUserIdAndActiveTrue(Long id, Long userId);
}
