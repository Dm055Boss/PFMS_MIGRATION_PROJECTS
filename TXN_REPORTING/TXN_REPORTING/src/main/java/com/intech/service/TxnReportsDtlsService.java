package com.intech.service;

import com.intech.domain.TxnAccountCandidate;
import com.intech.sql.QueryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TxnReportsDtlsService {

	private static final Logger log = LoggerFactory.getLogger(TxnReportsDtlsService.class);

	private final JdbcTemplate jdbcTemplate;
	private final QueryStore queryStore;

	public TxnReportsDtlsService(JdbcTemplate jdbcTemplate, QueryStore queryStore) {
		this.jdbcTemplate = jdbcTemplate;
		this.queryStore = queryStore;
	}

	public void upsertAfterFileCreation(List<TxnAccountCandidate> processed, String txnReqFileName, String status) {
		if (processed == null || processed.isEmpty()) {
			log.info("TXN_REPORTS_DTLS skipped (no processed accounts)");
			return;
		}

		String sql = queryStore.get("TXN_REPORTS_UPSERT");

		int ok = 0;
		for (TxnAccountCandidate c : processed) {
			jdbcTemplate.update(sql, c.getTxnId(), c.getAccountNo(), txnReqFileName, status);
			ok++;
		}

		log.info("TXN_REPORTS_DTLS upserted rows={} txnReqFile={}", ok, txnReqFileName);
	}
}
