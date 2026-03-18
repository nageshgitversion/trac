package com.investrac.auth.repository;

import com.investrac.auth.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<AuditLog> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(Long userId, Instant since);

    long countByUserIdAndActionAndSuccessFalseAndCreatedAtAfter(
        Long userId, AuditLog.AuditAction action, Instant since
    );
}
