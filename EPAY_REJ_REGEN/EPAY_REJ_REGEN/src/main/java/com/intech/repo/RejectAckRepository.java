package com.intech.repo;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.intech.config.RejectAckProperties;
import com.intech.exception.RejectAckException;
import com.intech.model.BatchInfo;
import com.intech.model.RejectedCreditRecord;

@Repository
public class RejectAckRepository {

	private static final Logger log = LogManager.getLogger(RejectAckRepository.class);

	private final JdbcTemplate jdbcTemplate;
	private final RejectAckProperties props;

	public RejectAckRepository(JdbcTemplate jdbcTemplate, RejectAckProperties props) {
		this.jdbcTemplate = jdbcTemplate;
		this.props = props;
	}

	/**
	 * Fetches all batches which have rejected payments and need a Reject XML.
	 */
	public List<BatchInfo> findEligibleBatches() {
		String sql = props.getSqlSelectBatches();
		log.debug("Executing batch selection SQL for reject ACK.");
		try {
			return jdbcTemplate.query(sql, new BatchInfoRowMapper());
		} catch (Exception ex) {
			log.error("Error while fetching eligible batches for Reject ACK", ex);
			throw new RejectAckException("Failed to fetch eligible batches", ex);
		}
	}

	/**
	 * Fetches all rejected credit records for a given batch.
	 */
	public List<RejectedCreditRecord> findRejectedCreditsForBatch(Long batchId) {
		String sql = props.getSqlSelectCreditsByBatch();
		log.debug("Executing credit selection SQL for batchId={}", batchId);
		try {
			return jdbcTemplate.query(sql, new RejectedCreditRowMapper(), batchId);
		} catch (Exception ex) {
			log.error("Error while fetching rejected credits for batchId={}", batchId, ex);
			throw new RejectAckException("Failed to fetch rejected credits for batch " + batchId, ex);
		}
	}

	/**
	 * Maps batch selection result set into BatchInfo.
	 */
	private static class BatchInfoRowMapper implements RowMapper<BatchInfo> {
		@Override
		public BatchInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
			Long batchId = rs.getLong("BATCH_ID");
			String batchNumber = rs.getString("BATCH_NUMBER");
			String paymentProd = rs.getString("PAYMENT_PROD");
			String bankCode = rs.getString("BANK_CODE");
			return new BatchInfo(batchId, batchNumber, paymentProd, bankCode);
		}
	}

	/**
	 * Maps credit-level result set into RejectedCreditRecord.
	 */
	private static class RejectedCreditRowMapper implements RowMapper<RejectedCreditRecord> {
		@Override
		public RejectedCreditRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
			RejectedCreditRecord rec = new RejectedCreditRecord();
			rec.setSeqCreditId(rs.getLong("SEQ_CREDIT_ID"));
			rec.setC2020(rs.getString("C2020"));
			rec.setC5756(rs.getString("C5756"));
			rec.setC2006(rs.getString("C2006"));
			rec.setC5569(rs.getString("C5569"));
			rec.setC6061(rs.getString("C6061"));
			rec.setUid(rs.getString("CREDIT_UID_STR"));
			rec.setBankIIN(rs.getString("BANKIIN"));
			rec.setC6081(rs.getString("C6081"));
			rec.setC5565(rs.getString("C5565"));

			BigDecimal amt = rs.getBigDecimal("C4038");
			if (amt == null) {
				amt = BigDecimal.ZERO;
			}
			rec.setAmount(amt);

			rec.setC3380(rs.getString("C3380"));
			rec.setC3375(rs.getString("C3375"));
			rec.setC3381(rs.getString("C3381"));
			rec.setC6346(rs.getString("C6346"));
			rec.setC6366(rs.getString("C6366"));
			rec.setC7495(rs.getString("C7495"));
			rec.setPmtRoute(rs.getString("PMT_ROUTE"));
			return rec;
		}
	}

	/**
	 * Updates FAILURE_REQUEST_ID for all given credit IDs with the generated reject
	 * MessageId.
	 */
	public void updateFailureRequestIdForCredits(String failureRequestId, List<Long> creditIds) {
		if (creditIds == null || creditIds.isEmpty()) {
			log.debug("No credit IDs to update FAILURE_REQUEST_ID for.");
			return;
		}

		String sql = props.getSqlUpdateFailureRequestId();
		if (sql == null || sql.isBlank()) {
			throw new IllegalStateException("reject.sql-update-failure-request-id is not configured");
		}

		log.info("Updating FAILURE_REQUEST_ID={} for {} credit records", failureRequestId, creditIds.size());

		jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
			
			
			@Override
			public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
				ps.setString(1, failureRequestId);
				ps.setLong(2, creditIds.get(i));
				System.out.println("update SUCCSESFULL.................");
			}

			@Override
			public int getBatchSize() {
				return creditIds.size();
			}
		});
	}
}
