// src/main/java/com/intech/cpsms/domain/repo/PaymentCreditMasterRepo.java
package com.intech.cpsms.domain.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.intech.cpsms.domain.entity.PaymentBatchMaster;
import com.intech.cpsms.domain.entity.PaymentCreditMaster;
import com.intech.cpsms.domain.entity.PaymentDebitMaster;

@Repository
public interface PaymentCreditMasterRepo extends JpaRepository<PaymentCreditMaster, Long> {

	List<PaymentCreditMaster> findByBatch(PaymentBatchMaster batch);

	List<PaymentCreditMaster> findByBatch_Id(Long batchId);

	List<PaymentCreditMaster> findByDebit(PaymentDebitMaster debit);

	List<PaymentCreditMaster> findByDebit_Id(Long debitId);
	
    Optional<PaymentCreditMaster> findByDebit_IdAndCpsmsCreditTran(Long debitId, String cpsmsCreditTran);

	
    boolean existsByCreditStan(String creditStan);
}
