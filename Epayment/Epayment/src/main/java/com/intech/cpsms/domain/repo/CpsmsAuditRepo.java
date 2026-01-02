// src/main/java/com/intech/cpsms/domain/repo/CpsmsAuditRepo.java
package com.intech.cpsms.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.intech.cpsms.domain.entity.CpsmsAudit;

@Repository
public interface CpsmsAuditRepo extends JpaRepository<CpsmsAudit, Long> {
	// Add finders as needed, e.g.:
	// List<CpsmsAudit> findByRequestMessageId(String requestMessageId);
}
