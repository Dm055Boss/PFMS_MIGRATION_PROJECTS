package com.intech.service;

import com.intech.sql.QueryStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Step 1: Selects accounts to be processed in the current run.
 *
 * Query used:
 *  ACCT_SELECT = SELECT DISTINCT ACCT_NO FROM TXN_ACCOUNT_STATUS WHERE DATA_REQ_FLAG = 'Y'
 */
@Service
public class AccountSelectionService {

    private final JdbcTemplate jdbcTemplate;
    private final QueryStore queryStore;

    public AccountSelectionService(JdbcTemplate jdbcTemplate, QueryStore queryStore) {
        this.jdbcTemplate = jdbcTemplate;
        this.queryStore = queryStore;
    }

    public List<String> selectAccounts() {
        String sql = queryStore.get("ACCT_SELECT");
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString(1));
    }
}
