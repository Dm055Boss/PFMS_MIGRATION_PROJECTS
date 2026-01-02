// src/main/java/com/intech/EpayIniFileGenerator/service/BatchQueryService.java
package com.intech.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intech.model.BatchPick;
import com.intech.model.CreditRow;
import com.intech.model.DebitRow;
import com.intech.support.SqlStore;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class BatchQueryService {

	@PersistenceContext
	private EntityManager em;
	private final SqlStore sql;

	public BatchQueryService(SqlStore sql) {
		this.sql = sql;
	}

	@Transactional(readOnly = true)
	public List<BatchPick> findEligibleBatches() {
		String sqlText = sql.get("batch.distinct");
		@SuppressWarnings("unchecked")
		List<Object[]> rows = em.createNativeQuery(sqlText).getResultList();
		List<BatchPick> out = new ArrayList<>();
		for (Object[] r : rows) {
			out.add(new BatchPick(((Number) r[0]).longValue(), // batchId
					(String) r[1], // batchNumber
					(String) r[2], // product
					(String) r[3] // bankCode
			));
		}
		return out;
	}

	@Transactional(readOnly = true)
	public DebitRow loadDebitRow(Long batchId) {
		String sqlText = sql.get("debit.byBatch");
		Object[] r = (Object[]) em.createNativeQuery(sqlText).setParameter("batchId", batchId).getSingleResult();
		return new DebitRow((String) r[0], (String) r[1], (String) r[2], (String) r[3], (String) r[4], (String) r[5]);
	}

	@Transactional(readOnly = true)
	public List<CreditRow> loadCreditRows(Long batchId) {
		String sqlText = sql.get("credit.byBatch");
		@SuppressWarnings("unchecked")
		List<Object[]> rs = em.createNativeQuery(sqlText).setParameter("batchId", batchId).getResultList();
		List<CreditRow> out = new ArrayList<>();
		for (Object[] r : rs) {
			out.add(new CreditRow(((Number) r[0]).longValue(), // seqCreditId (optional)
					(String) r[1], (String) r[2], (String) r[3], (String) r[4], (String) r[5], (String) r[6],
					(String) r[7], (String) r[8], (String) r[9], (String) r[10], (String) r[11], (String) r[12],
					(String) r[13]));
		}
		return out;
	}
}
