package com.intech.service;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.intech.domain.CounterpartyInfo;
import com.intech.domain.TransactionDetail;
import com.intech.exception.DataFetchException;
import com.intech.sql.QueryStore;

/**
 * Step 3 and Step 4: - CNT query: checks how many transactions exist for the
 * account for txnDate - TXN query: fetches transaction details when count > 0
 *
 * Legacy enrichment: - XCNT: count opposite leg rows for (part_tran_type,
 * tran_id, tran_date) - If XCNT == 1 then KBDT: fetch opposite party account
 * details (foracid, name, sol, bank/group)
 */
@Service
public class TransactionService {

	private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

	private static final String BR_PREFIX = "056-"; // legacy prefix used in old code

	private final JdbcTemplate jdbcTemplate;
	private final QueryStore queryStore;

	public TransactionService(JdbcTemplate jdbcTemplate, QueryStore queryStore) {
		this.jdbcTemplate = jdbcTemplate;
		this.queryStore = queryStore;
	}

	public int loadTransactionCount(String foracid, String txnDateDdMmYyyy) {
		String sql = queryStore.get("CNT");
		try {
			Integer cnt = jdbcTemplate.queryForObject(sql, Integer.class, txnDateDdMmYyyy, foracid);
			return (cnt == null) ? 0 : cnt;
		} catch (Exception e) {
			throw new DataFetchException("CNT query failed for account=" + foracid + ", txnDate=" + txnDateDdMmYyyy, e);
		}
	}

	public List<TransactionDetail> loadTransactions(String foracid, String txnDateDdMmYyyy) {
		String sql = queryStore.get("TXN");
		try {
			List<TransactionDetail> txns = jdbcTemplate.query(sql,
					(rs, rowNum) -> new TransactionDetail(rs.getString(1), rs.getString(2), rs.getString(3),
							rs.getString(4), rs.getString(5), rs.getString(6),
							rs.getBigDecimal(7) == null ? BigDecimal.ZERO : rs.getBigDecimal(7), rs.getString(8),
							rs.getString(9), rs.getString(10), rs.getString(11), rs.getString(12)),
					txnDateDdMmYyyy, foracid);

			// Enrich each transaction with counterparty details (legacy behavior)
			enrichCounterparty(txns, txnDateDdMmYyyy);

			return txns;
		} catch (Exception e) {
			throw new DataFetchException("TXN query failed for account=" + foracid + ", txnDate=" + txnDateDdMmYyyy, e);
		}
	}

	private void enrichCounterparty(List<TransactionDetail> txns, String txnDateDdMmYyyy) {
		if (txns == null || txns.isEmpty())
			return;

		final String xcntSql = queryStore.get("XCNT");
		final String kbdtSql = queryStore.get("KBDT");

		for (TransactionDetail t : txns) {
			String partType = safe(t.getPartTranType());
			String tranId = safe(t.getTranId());
			if (partType.isBlank() || tranId.isBlank())
				continue;

			int xcnt = loadXcnt(xcntSql, partType, tranId, txnDateDdMmYyyy);

			// Legacy rule: fill Dr/Cr fields only when exactly one opposite leg exists
			if (xcnt != 1)
				continue;

			CounterpartyInfo cp = loadKbdt(kbdtSql, partType, tranId, txnDateDdMmYyyy);
			if (cp != null) {
				t.setCounterpartyInfo(cp);
			}
		}
	}

	private int loadXcnt(String xcntSql, String partType, String tranId, String txnDateDdMmYyyy) {
		try {
			Integer v = jdbcTemplate.queryForObject(xcntSql, Integer.class, partType, tranId, txnDateDdMmYyyy);
			return v == null ? 0 : v;
		} catch (EmptyResultDataAccessException e) {
			return 0; // GROUP BY can return no rows
		} catch (Exception e) {
			// XCNT should not kill the whole job; log and skip enrichment
			log.warn("XCNT_SKIP tranId={} reason={}", tranId, e.getMessage());
			return 0;
		}
	}

	private CounterpartyInfo loadKbdt(String kbdtSql, String partType, String tranId, String txnDateDdMmYyyy) {
		try {
			List<CounterpartyInfo> rows = jdbcTemplate.query(kbdtSql, (rs, rowNum) -> {
				String acctNo = safe(rs.getString(1));
				String acctName = safe(rs.getString(2));
				String solId = safe(rs.getString(3));
				String bankName = safe(rs.getString(4));
				return new CounterpartyInfo(acctNo, acctName, bankName, BR_PREFIX + solId);
			}, partType, tranId, txnDateDdMmYyyy);

			return rows.isEmpty() ? null : rows.get(0);
		} catch (Exception e) {
			log.warn("KBDT_SKIP tranId={} reason={}", tranId, e.getMessage());
			return null;
		}
	}

	private String safe(String v) {
		return v == null ? "" : v.trim();
	}
}
