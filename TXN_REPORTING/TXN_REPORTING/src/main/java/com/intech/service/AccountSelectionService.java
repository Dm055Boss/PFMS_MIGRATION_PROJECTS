package com.intech.service;

import com.intech.domain.TxnAccountCandidate;
import com.intech.sql.QueryStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountSelectionService {

	private final JdbcTemplate jdbcTemplate;
	private final QueryStore queryStore;

	public AccountSelectionService(JdbcTemplate jdbcTemplate, QueryStore queryStore) {
		this.jdbcTemplate = jdbcTemplate;
		this.queryStore = queryStore;
	}

	public List<TxnAccountCandidate> selectAccounts() {
		String sql = queryStore.get("ACCT_SELECT");
		return jdbcTemplate.query(sql, (rs, rowNum) -> new TxnAccountCandidate(rs.getLong(1), rs.getString(2)));
	}
}
