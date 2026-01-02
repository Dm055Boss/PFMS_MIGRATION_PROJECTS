// src/main/java/com/intech/epayackreader/repository/PaymentBatchMasterRepository.java
package com.intech.EpayACKReaders.repo;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.intech.EpayACKReaders.model.PaymentBatchMaster;

@Repository
public interface PaymentBatchMasterRepository extends JpaRepository<PaymentBatchMaster, Long> {

    Logger LOGGER = LogManager.getLogger(PaymentBatchMasterRepository.class);

    /**
     * Find batch by BATCH_NUMBER (InfId.@Id in ACK XML).
     */
    Optional<PaymentBatchMaster> findByBatchNumber(String batchNumber);
}
