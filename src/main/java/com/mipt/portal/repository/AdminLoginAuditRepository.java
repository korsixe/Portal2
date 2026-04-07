package com.mipt.portal.repository;

import com.mipt.portal.entity.AdminLoginAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminLoginAuditRepository extends JpaRepository<AdminLoginAudit, Long> {
}
