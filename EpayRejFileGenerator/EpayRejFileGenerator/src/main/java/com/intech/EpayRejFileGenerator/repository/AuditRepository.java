package com.intech.EpayRejFileGenerator.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.intech.EpayRejFileGenerator.config.RejectAckProperties;
import com.intech.EpayRejFileGenerator.exception.RejectAckException;

@Repository
public class AuditRepository {

    private static final Logger log = LogManager.getLogger(AuditRepository.class);

    private final JdbcTemplate jdbcTemplate;
    private final RejectAckProperties props;

    public AuditRepository(JdbcTemplate jdbcTemplate, RejectAckProperties props) {
        this.jdbcTemplate = jdbcTemplate;
        this.props = props;
    }

    /**
     * Inserts a record into TBL_CPSMS_AUDIT_TABLE for this reject file.
     */
    public void insertAuditRecord(String requestMessageId,
                                  String batchNumber,
                                  String batchStatus,
                                  String remarks1,
                                  String remarks2) {
        String sql = props.getSqlInsertAudit();
        System.out.println("audit query calleed..............");
        try {
            int count = jdbcTemplate.update(sql,
                    requestMessageId,
                    batchNumber,
                    batchStatus,
                    remarks1,
                    remarks2
            );
            
            log.info("Inserted {} row(s) into TBL_CPSMS_AUDIT_TABLE for messageId={}", count, requestMessageId);
        } catch (Exception ex) {
            log.error("Error while inserting audit record for messageId={}", requestMessageId, ex);
            throw new RejectAckException("Failed to insert audit record", ex);
        }
    }
}
