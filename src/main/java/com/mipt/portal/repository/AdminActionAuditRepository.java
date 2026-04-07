package com.mipt.portal.repository;

import com.mipt.portal.entity.AdminActionAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminActionAuditRepository extends JpaRepository<AdminActionAudit, Long> {
}
