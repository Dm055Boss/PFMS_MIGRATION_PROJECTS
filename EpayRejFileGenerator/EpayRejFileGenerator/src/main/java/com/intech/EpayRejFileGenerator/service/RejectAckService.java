package com.intech.EpayRejFileGenerator.service;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.intech.EpayRejFileGenerator.config.RejectAckProperties;
import com.intech.EpayRejFileGenerator.exception.BatchProcessingException;
import com.intech.EpayRejFileGenerator.exception.RejectAckException;
import com.intech.EpayRejFileGenerator.model.BatchInfo;
import com.intech.EpayRejFileGenerator.model.RejectedCreditRecord;
import com.intech.EpayRejFileGenerator.repository.AuditRepository;
import com.intech.EpayRejFileGenerator.repository.RejectAckRepository;
import com.intech.EpayRejFileGenerator.xml.RejectXmlWriter;

@Service
public class RejectAckService {

	private static final Logger log = LogManager.getLogger(RejectAckService.class);

	private final RejectAckRepository rejectAckRepository;
	private final AuditRepository auditRepository;
	private final RejectFileNameService fileNameService;
	private final RejectXmlWriter xmlWriter;
	private final RejectAckProperties props;

	public RejectAckService(RejectAckRepository rejectAckRepository, AuditRepository auditRepository,
			RejectFileNameService fileNameService, RejectXmlWriter xmlWriter, RejectAckProperties props) {
		this.rejectAckRepository = rejectAckRepository;
		this.auditRepository = auditRepository;
		this.fileNameService = fileNameService;
		this.xmlWriter = xmlWriter;
		this.props = props;
	}

	/**
	 * Entry method called by scheduler. Iterates over all eligible batches and
	 * generates reject XML files.
	 */
	public void generateRejectFiles() {
		List<BatchInfo> batches = rejectAckRepository.findEligibleBatches();
		if (batches == null || batches.isEmpty()) {
			log.info("No batches found for Reject ACK generation.");
			return;
		}

		log.info("Found {} batch(es) for Reject ACK generation.", batches.size());

		for (BatchInfo batch : batches) {
			try {
				processSingleBatch(batch);
			} catch (BatchProcessingException ex) {
				// Log and continue with next batch – do not stop entire job
				log.error("Failed to process Reject ACK for batch {}: {}", batch.getBatchNumber(), ex.getMessage(), ex);
			}
		}
	}

	/**
	 * Processes one batch: fetch credits, build XML, write file, insert audit.
	 */
	private void processSingleBatch(BatchInfo batch) {
		log.info("Processing Reject ACK for batch: {}", batch);

		List<RejectedCreditRecord> credits = rejectAckRepository.findRejectedCreditsForBatch(batch.getBatchId());

		if (credits == null || credits.isEmpty()) {
			log.warn("No rejected credits found for batch {}. Skipping XML generation.", batch.getBatchNumber());
			return;
		}

		// Calculate totals
		BigDecimal totalAmt = credits.stream().map(RejectedCreditRecord::getAmount).filter(a -> a != null)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		int recordCount = credits.size();

		String messageId = null;
		try {
			// Generate messageId & increment counter
			messageId = fileNameService.generateMessageId(batch.getPaymentProduct(), batch.getBankCode());

			// Generate XML file
			Path xmlPath = xmlWriter.writeRejectXml(messageId, batch.getPaymentProduct(), batch.getBankCode(),
					props.getBankName(), props.getDestination(), props.getSource(), batch.getBatchNumber(), totalAmt,
					recordCount, credits);

			log.info("Reject XML file created for batch {} at path {}", batch.getBatchNumber(), xmlPath);
			
			List<Long> creditId=credits.stream().map(RejectedCreditRecord::getSeqCreditId).toList();

			// 2) Update FAILURE_REQUEST_ID in PAYMENT_CREDIT_MASTER
			rejectAckRepository.updateFailureRequestIdForCredits(messageId, creditId);

			// Insert audit record (similar to legacy)
			auditRepository.insertAuditRecord(messageId, batch.getBatchNumber(), "PI", // BATCHSTATUS
					"REJ", // remarks1
					"Manual Generated" // remarks2
			);

		} catch (RejectAckException ex) {
			// XML or audit failure – rollback counter and wrap in BatchProcessingException
			if (messageId != null) {
				fileNameService.rollbackLastIncrement();
			}
			throw new BatchProcessingException("Error while processing batch " + batch.getBatchNumber(), ex);
		} catch (Exception ex) {
			// Any other unexpected error
			if (messageId != null) {
				fileNameService.rollbackLastIncrement();
			}
			throw new BatchProcessingException("Unexpected error while processing batch " + batch.getBatchNumber(), ex);
		}
	}
}
