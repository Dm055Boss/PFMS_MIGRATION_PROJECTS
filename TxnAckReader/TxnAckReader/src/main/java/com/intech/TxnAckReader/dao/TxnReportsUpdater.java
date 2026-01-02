package com.intech.TxnAckReader.dao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.intech.TxnAckReader.service.AckXmlParser.AckData;

@Service
public class TxnReportsUpdater {

    private final JdbcTemplate jdbcTemplate;

    @Value("${sql.txnReports.updateByTxnReqFn}")
    private String updateSql;

    public TxnReportsUpdater(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int updateTxnReports(AckData ack) {
        return jdbcTemplate.update(
                updateSql,
                ack.responseCode,
                ack.errorCodeOrNull,
                ack.ackMessageId,
                ack.txnReqFn
        );
    }
}
