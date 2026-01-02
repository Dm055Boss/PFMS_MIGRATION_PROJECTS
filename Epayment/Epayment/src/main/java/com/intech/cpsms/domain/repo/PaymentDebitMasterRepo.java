// src/main/java/com/intech/cpsms/domain/repo/PaymentDebitMasterRepo.java
package com.intech.cpsms.domain.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.intech.cpsms.domain.entity.PaymentBatchMaster;
import com.intech.cpsms.domain.entity.PaymentDebitMaster;

@Repository
public interface PaymentDebitMasterRepo extends JpaRepository<PaymentDebitMaster, Long> {

	List<PaymentDebitMaster> findByBatch(PaymentBatchMaster batch);
	
    Optional<PaymentDebitMaster> findByBatch_IdAndCpsmsDebitTranId(Long batchId, String cpsmsDebitTranId);


	List<PaymentDebitMaster> findByBatch_Id(Long batchId);
	
	boolean existsByDebitStan(String debitStan);
}
