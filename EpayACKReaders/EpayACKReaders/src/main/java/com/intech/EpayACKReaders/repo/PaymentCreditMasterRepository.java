// src/main/java/com/intech/epayackreader/repository/PaymentCreditMasterRepository.java
package com.intech.EpayACKReaders.repo;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.intech.EpayACKReaders.model.PaymentBatchMaster;
import com.intech.EpayACKReaders.model.PaymentCreditMaster;

@Repository
public interface PaymentCreditMasterRepository extends JpaRepository<PaymentCreditMaster, Long> {

	Logger LOGGER = LogManager.getLogger(PaymentCreditMasterRepository.class);

	/**
	 * INI ACK: Data.MsgId == PAYMENT_INITI_REQ_ID
	 */
	List<PaymentCreditMaster> findByBatchAndPaymentInitiReqId(PaymentBatchMaster batch, String paymentInitiReqId);

	/**
	 * SUC ACK: Data.MsgId == PAYMENT_SUCCESS_REQ_ID
	 */
	List<PaymentCreditMaster> findByBatchAndPaymentSuccessReqId(PaymentBatchMaster batch, String paymentSuccessReqId);

	/**
	 * REJ ACK: Data.MsgId == FAILURE_REQUEST_ID
	 */
	List<PaymentCreditMaster> findByBatchAndFailureRequestId(PaymentBatchMaster batch, String failureRequestId);

	// ========= Record-level queries (with CPSMS_CREDIT_TRAN) =========
	// Used for GpSts="E" (error) when <RcrdId> is present.

	List<PaymentCreditMaster> findByBatchAndPaymentInitiReqIdAndCpsmsCreditTran(PaymentBatchMaster batch,
			String paymentInitiReqId, String cpsmsCreditTran);

	List<PaymentCreditMaster> findByBatchAndPaymentSuccessReqIdAndCpsmsCreditTran(PaymentBatchMaster batch,
			String paymentSuccessReqId, String cpsmsCreditTran);

	List<PaymentCreditMaster> findByBatchAndFailureRequestIdAndCpsmsCreditTran(PaymentBatchMaster batch,
			String failureRequestId, String cpsmsCreditTran);

}
