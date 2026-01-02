package com.intech.EXAckReader.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.intech.EXAckReader.model.AckExceptionRecord;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

/**
 * DB updater using JPA EntityManager + native SQL (SQL comes from external
 * props).
 */
@Repository
public class TxnReportsDtlsDao {
	private static final Logger log = LoggerFactory.getLogger(TxnReportsDtlsDao.class);

	private final EntityManager entityManager;

	@Value("${sql.updateTxnReportsAck}")
	private String updateSql;

	public TxnReportsDtlsDao(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	/**
	 * Updates TXN_REPORTS_DTLS using (OriginalMessageId + AccountNumber).
	 * EX_RCV_DATE is SYSDATE in SQL itself.
	 */
	@Transactional
	public int updateFromAck(AckExceptionRecord r) {
		Query q = entityManager.createNativeQuery(updateSql);

		q.setParameter("exFilename", r.messageId());
		q.setParameter("remarks", r.cleanedRemarks());
		q.setParameter("reconType", r.reconciliationType());
		q.setParameter("accountNo", r.accountNumber());
		q.setParameter("origMessageId", r.normalizedOriginalMessageId());

		int updated = q.executeUpdate();

		if (updated == 0) {
			log.warn("No rows updated for AccountNo={} OriginalMessageId={}", r.accountNumber(),
					r.normalizedOriginalMessageId());
		}
		return updated;
	}
}
