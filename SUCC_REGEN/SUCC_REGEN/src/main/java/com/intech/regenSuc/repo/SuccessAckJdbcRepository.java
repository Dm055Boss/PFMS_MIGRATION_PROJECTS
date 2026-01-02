package com.intech.regenSuc.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.intech.regenSuc.service.dto.BatchInfo;

@Repository
public class SuccessAckJdbcRepository {

    private final NamedParameterJdbcTemplate jdbc;

    private final String selectBatchesSql;
    private final String selectCreditsSql;
    private final String updateCreditsSql;
    private final String countAuditSql;
    private final String debitStatusSql;
    private final String insertAuditFirstSql;
    private final String insertAuditResendSql;

    public SuccessAckJdbcRepository(NamedParameterJdbcTemplate jdbc,@Qualifier("SuccRegenProps")
                                    Properties props) {

//        this.jdbc = jdbc;
//        this.selectBatchesSql = props.getProperty("success-ack.sql.select-batches");
//        this.selectCreditsSql = props.getProperty("success-ack.sql.select-credits");
//        this.updateCreditsSql = props.getProperty("success-ack.sql.update-credits");
//        this.countAuditSql = props.getProperty("success-ack.sql.count-audit-for-batch");
//        this.debitStatusSql = props.getProperty("success-ack.sql.debit-status-for-batch");
//        this.insertAuditFirstSql = props.getProperty("success-ack.sql.insert-audit-first");
//        this.insertAuditResendSql = props.getProperty("success-ack.sql.insert-audit-resend");
    	
    	this.jdbc = jdbc;
        this.selectBatchesSql = props.getProperty("success.select-batches");
        this.selectCreditsSql = props.getProperty("success.select-credits");
        this.updateCreditsSql = props.getProperty("success.update-credits");
        this.countAuditSql = props.getProperty("success.count-audit-for-batch");
        this.debitStatusSql = props.getProperty("success.debit-status-for-batch");
        this.insertAuditFirstSql = props.getProperty("success.insert-audit-first");
        this.insertAuditResendSql = props.getProperty("success.insert-audit-resend");
    }

    // FETCH BATCHES
    public List<BatchInfo> findEligibleBatches() {
        return jdbc.query(selectBatchesSql, (rs, i) ->
                new BatchInfo(
                        rs.getLong("batchId"),
                        rs.getString("batchNumber"),
                        rs.getString("paymentProd"),
                        rs.getString("bankCode"),
                        rs.getInt("rc")
                ));
    }

    // FETCH CREDITS
    public List<CreditInfo> findCreditsForBatch(Long batchId) {
        Map<String, Object> params = Map.of("batchId", batchId);
        return jdbc.query(selectCreditsSql, params, new CreditRowMapper());
    }

    // UPDATE CREDIT REQ ID
    public int updateCreditsWithMsgId(List<String> creditTrans, String msgId) {
        MapSqlParameterSource p = new MapSqlParameterSource();
        p.addValue("msgId", msgId);
        p.addValue("creditTrans", creditTrans);
        return jdbc.update(updateCreditsSql, p);
    }

    // FETCH DEBIT STATUS
    public String findDebitStatusForBatch(long batchId) {
        List<String> list = jdbc.query(
                debitStatusSql,
                Map.of("batchId", batchId),
                (rs, i) -> rs.getString("DEBIT_STATUS")
        );
        return list.isEmpty() ? null : list.get(0);
    }

    // COUNT AUDIT
    public int countAuditRecords(String batchNumber, String reqPrefix) {
        Map<String, Object> params = new HashMap<>();
        params.put("batchNumber", batchNumber);
        params.put("reqMsgPrefix", reqPrefix + "%");
        Integer cnt = jdbc.queryForObject(countAuditSql, params, Integer.class);
        return cnt == null ? 0 : cnt;
    }

    // INSERT FIRST AUDIT
    public int insertFirstAudit(String msgId, String batchNumber, String batchStatus) {
        Map<String, Object> p = new HashMap<>();
        p.put("reqMsgId", msgId);
        p.put("batchNumber", batchNumber);
        p.put("batchStatus", batchStatus);
        return jdbc.update(insertAuditFirstSql, p);
    }

    // INSERT RESEND AUDIT
    public int insertResendAudit(String msgId, String batchNumber,
                                 String batchStatus, int attempt) {
        Map<String, Object> p = new HashMap<>();
        p.put("reqMsgId", msgId);
        p.put("batchNumber", batchNumber);
        p.put("batchStatus", batchStatus);
        p.put("remarks2", "Resend Payment Success File-" + attempt);
        return jdbc.update(insertAuditResendSql, p);
    }

    private static class CreditRowMapper implements RowMapper<CreditInfo> {
        @Override
        public CreditInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new CreditInfo(
                    rs.getLong("id"),
                    rs.getString("batchNumber"),
                    rs.getString("paymentProd"),
                    rs.getString("bankCode"),
                    rs.getString("cpsmsCreditTran"),
                    rs.getString("creditIfsc"),
                    rs.getString("creditAccountNumber"),
                    rs.getString("creditBankIin"),
                    rs.getString("creditUid"),
                    rs.getString("creditAccountName"),
                    rs.getBigDecimal("creditAmount"),
                    rs.getString("creditStatus"),
                    rs.getString("pmtMtd"),
                    rs.getTimestamp("tranDateFt"),
                    rs.getString("tranIdFt"),
                    rs.getString("initiatingUtr"),
                    rs.getString("debitTranId")
            );
        }
    }
}
