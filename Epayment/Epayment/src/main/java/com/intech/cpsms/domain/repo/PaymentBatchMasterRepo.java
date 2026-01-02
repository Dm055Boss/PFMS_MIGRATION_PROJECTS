// src/main/java/com/intech/cpsms/domain/repo/PaymentBatchMasterRepo.java
package com.intech.cpsms.domain.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.intech.cpsms.domain.entity.PaymentBatchMaster;

@Repository
public interface PaymentBatchMasterRepo extends JpaRepository<PaymentBatchMaster, Long> {

	Optional<PaymentBatchMaster> findByRequestMessageId(String requestMessageId);

	boolean existsByRequestMessageId(String requestMessageId);

	Optional<PaymentBatchMaster> findByBatchNumber(String batchNumber);

    long countByRequestMessageIdAndBatchNumber(String requestMessageId, String batchNumber);
    


}
