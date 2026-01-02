package com.intech.service;

import com.intech.domain.TransactionDetail;
import com.intech.exception.DataFetchException;
import com.intech.sql.QueryStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Step 3 and Step 4:
 * - CNT query: checks how many transactions exist for the account for txnDate
 * - TXN query: fetches transaction details when count > 0
 */
@Service
public class TransactionService {

    private final JdbcTemplate jdbcTemplate;
    private final QueryStore queryStore;

    public TransactionService(JdbcTemplate jdbcTemplate, QueryStore queryStore) {
        this.jdbcTemplate = jdbcTemplate;
        this.queryStore = queryStore;
    }

    /**
     * Returns number of transactions. If DB returns null, returns 0.
     */
    public int loadTransactionCount(String foracid, String txnDateDdMmYyyy) {
        String sql = queryStore.get("CNT");
        try {
            Integer cnt = jdbcTemplate.queryForObject(sql, Integer.class, txnDateDdMmYyyy, foracid);
            return (cnt == null) ? 0 : cnt;
        } catch (Exception e) {
            throw new DataFetchException("CNT query failed for account=" + foracid, e);
        }
    }

    /**
     * Fetches transaction rows for the account and date.
     */
    public List<TransactionDetail> loadTransactions(String foracid, String txnDateDdMmYyyy) {
        String sql = queryStore.get("TXN");
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> new TransactionDetail(
                    rs.getString(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4),
                    rs.getString(5),
                    rs.getString(6),
                    rs.getBigDecimal(7) == null ? BigDecimal.ZERO : rs.getBigDecimal(7),
                    rs.getString(8),
                    rs.getString(9),
                    rs.getString(10),
                    rs.getString(11),
                    rs.getString(12)
            ), txnDateDdMmYyyy, foracid);
        } catch (Exception e) {
            throw new DataFetchException("TXN query failed for account=" + foracid, e);
        }
    }
}
