package com.intech.service;

import com.intech.domain.BalanceInfo;
import com.intech.exception.DataFetchException;
import com.intech.sql.QueryStore;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

/**
 * Step 2: Loads account tag attributes (BAL query) from GAM + SOL.
 *
 * Expected named parameters:
 * - :txndate (ddMMyyyy)
 * - :foracid
 */
@Service
public class BalanceService {

    private final NamedParameterJdbcTemplate namedJdbc;
    private final QueryStore queryStore;

    public BalanceService(NamedParameterJdbcTemplate namedJdbc, QueryStore queryStore) {
        this.namedJdbc = namedJdbc;
        this.queryStore = queryStore;
    }

    public Optional<BalanceInfo> loadBalance(String foracid, String txnDateDdMmYyyy) {
        String sql = queryStore.get("BAL");

        Map<String, Object> params = Map.of(
                "foracid", foracid,
                "txndate", txnDateDdMmYyyy
        );

        try {
            return Optional.ofNullable(namedJdbc.query(sql, params, rs -> {
                if (!rs.next()) {
                    return null;
                }
                String acct = rs.getString(1);
                String bsr = rs.getString(2);
                String closeDate = rs.getString(3);
                BigDecimal opening = rs.getBigDecimal(4);
                BigDecimal closing = rs.getBigDecimal(5);
                return new BalanceInfo(acct, bsr, closeDate, opening, closing);
            }));
        } catch (Exception e) {
            throw new DataFetchException("BAL query failed for account=" + foracid, e);
        }
    }
}
