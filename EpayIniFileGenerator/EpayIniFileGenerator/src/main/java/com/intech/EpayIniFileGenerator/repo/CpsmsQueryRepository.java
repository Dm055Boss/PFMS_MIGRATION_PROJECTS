// src/main/java/com/intech/EpayIniFileGenerator/repo/CpsmsQueryRepository.java
package com.intech.EpayIniFileGenerator.repo;

import com.intech.EpayIniFileGenerator.model.BatchPick;
import com.intech.EpayIniFileGenerator.model.CreditRow;
import com.intech.EpayIniFileGenerator.model.DebitRow;
import com.intech.EpayIniFileGenerator.support.SqlStore;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CpsmsQueryRepository {

	private static final Logger log = LogManager.getLogger(CpsmsQueryRepository.class);

	@PersistenceContext
	private EntityManager em;

	private final SqlStore sql;

	public CpsmsQueryRepository(SqlStore sql) {
		this.sql = sql;
	}

	/**
	 * Distinct batches (optionally excludes a product if your SQL uses :exProd).
	 */
	public List<BatchPick> findDistinctBatches(String excludeProduct) {
		final String qtxt = sql.get("batch.distinct"); // external: sql.batch.distinct=...
		Query q = em.createNativeQuery(qtxt);
		if (qtxt.contains(":exProd")) {
			q.setParameter("exProd", excludeProduct == null ? "" : excludeProduct.toUpperCase());
		}

		@SuppressWarnings("unchecked")
		List<Object[]> rows = q.getResultList();
		log.debug("findDistinctBatches: resultCount={}", rows.size());

		List<BatchPick> out = new ArrayList<>(rows.size());
		for (Object[] r : rows) {
			out.add(new BatchPick(toLong(r[0]), // SEQ_BATCH_ID
					str(r[1]), // BATCH_NUMBER
					str(r[2]), // PRODUCT
					str(r[3]) // BANKCODE
			));
		}
		return out;
	}

	/**
	 * One debit row per batch (most recent), includes C6346 and AuthMode like
	 * legacy.
	 */
	public DebitRow findDebit(Long batchId) {
		final String qtxt = sql.get("debit.byBatch"); // external: sql.debit.byBatch=...
		Query q = em.createNativeQuery(qtxt);
		// Bind only :batchId (standardized in external SQL)
		if (qtxt.contains(":batchId"))
			q.setParameter("batchId", batchId);

		@SuppressWarnings("unchecked")
		List<Object[]> rows = q.getResultList();
		if (rows.isEmpty())
			return null;

		Object[] r = rows.get(0);
		// Expected: IFSC, ACC_NO, DEBIT_AMT, CPSMS_DEBIT_TRAN, INIT_UTR, TS, C6346,
		// AUTH_MODE
		return new DebitRow(str(r[0]), str(r[1]), str(r[2]), str(r[3]), str(r[4]), str(r[5]), str(r[6]), str(r[7]));
	}

	/** Credit rows for batch. Debit failure (DBFL) override is passed in. */
	public List<CreditRow> findCredits(Long batchId, boolean debitFailed) {
		final String qtxt = sql.get("credit.byBatch"); // external: sql.credit.byBatch=...
		Query q = em.createNativeQuery(qtxt);
		if (qtxt.contains(":batchId"))
			q.setParameter("batchId", batchId);
		if (qtxt.contains(":dbfl"))
			q.setParameter("dbfl", debitFailed ? 1 : 0);

		@SuppressWarnings("unchecked")
		List<Object[]> rows = q.getResultList();
		List<CreditRow> out = new ArrayList<>(rows.size());
		for (Object[] r : rows) {
			out.add(CreditRow.fromRow(r)); // tolerant mapper (14/15/16 cols)
		}
		return out;
	}

	/** Mark PAYMENT_INITI_REQ_ID for all credits in the batch. */
	public int markInitiReqForBatch(Long batchId, String msgId) {
		final String qtxt = sql.get("update.markInitiByBatch"); // external: sql.update.markInitiByBatch=...
		Query q = em.createNativeQuery(qtxt);
		if (qtxt.contains(":batchId"))
			q.setParameter("batchId", batchId);
		if (qtxt.contains(":msgId"))
			q.setParameter("msgId", msgId);
		return q.executeUpdate();
	}

	public void flush() {
		em.flush();
	}

	private static String str(Object o) {
		return o == null ? "" : o.toString();
	}

	private static Long toLong(Object o) {
		if (o == null)
			return null;
		if (o instanceof Number n)
			return n.longValue();
		return Long.valueOf(o.toString());
	}
}
