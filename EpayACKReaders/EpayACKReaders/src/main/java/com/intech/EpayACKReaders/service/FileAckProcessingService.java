// src/main/java/com/intech/epayackreader/service/FileAckProcessingService.java
package com.intech.EpayACKReaders.service;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intech.EpayACKReaders.bean.FileAck;
import com.intech.EpayACKReaders.exceptions.AckProcessingException;
import com.intech.EpayACKReaders.model.PaymentBatchMaster;
import com.intech.EpayACKReaders.model.PaymentCreditMaster;
import com.intech.EpayACKReaders.repo.PaymentBatchMasterRepository;
import com.intech.EpayACKReaders.repo.PaymentCreditMasterRepository;
import com.intech.EpayACKReaders.xml.FileAckXmlParser;

/**
 * Core business logic: - Parse ACK XML (FileAck) - Determine file type
 * (INI/SUC/REJ) - For each <Data>: - Find batch (BATCH_NUMBER) - Find relevant
 * credit records (group-level or record-level via CPSMS_CREDIT_TRAN) - Update
 * INI/SUC/REJ ACK columns
 */
@Service
public class FileAckProcessingService {

	private static final Logger LOGGER = LogManager.getLogger(FileAckProcessingService.class);

	private final FileAckXmlParser xmlParser;
	private final PaymentBatchMasterRepository batchRepository;
	private final PaymentCreditMasterRepository creditRepository;

	public FileAckProcessingService(FileAckXmlParser xmlParser, PaymentBatchMasterRepository batchRepository,
			PaymentCreditMasterRepository creditRepository) {
		this.xmlParser = xmlParser;
		this.batchRepository = batchRepository;
		this.creditRepository = creditRepository;
	}

	/**
	 * Processes a single ACK/NACK file. Entire file is processed in one
	 * transaction; any failure rolls back updates for that file.
	 */
	@Transactional
	public void processFile(Path path) {
		LOGGER.info("Processing ACK file: {}", path);

		FileAck fileAck = xmlParser.parse(path);

		// Root MsgId is used as ACK file name (without .xml)
		String ackFileNameFromMsgId = fileAck.getMsgId();

		FileAck.FileAckDetails fileAckDetails = fileAck.getFileAckDetails();
		if (fileAckDetails == null || fileAckDetails.getMsgTp() == null) {
			throw new AckProcessingException("FileAckDetails/MsgTp is null for file: " + path);
		}

		FileAck.FileAckDetails.MsgTp msgTp = fileAckDetails.getMsgTp();
		String msgNm = msgTp.getMsgNm(); // e.g. INIPAYACK / SUCPAYACK / REJPAYACK
		AckFileType fileType = AckFileTypeResolver.detect(msgNm, path.getFileName().toString());

		LOGGER.info("ACK file '{}' detected as type {} with MsgNm='{}'", ackFileNameFromMsgId, fileType, msgNm);

		if (msgTp.getData() == null || msgTp.getData().isEmpty()) {
			LOGGER.warn("No <Data> records found in ACK file: {}", path);
			return;
		}

		for (FileAck.FileAckDetails.MsgTp.Data data : msgTp.getData()) {
			try {
				processDataRecord(data, fileType, ackFileNameFromMsgId);
			} catch (Exception e) {
				// Log at record-level but do not fail whole file; continue with other <Data>
				LOGGER.error("Error processing <Data> record in ACK file '{}': {}", ackFileNameFromMsgId,
						e.getMessage(), e);
			}
		}

		LOGGER.info("Successfully processed ACK file: {}", path);
	}

	/**
	 * Process one <Data> record inside the ACK file. For GpSts="S": group-level
	 * update. For GpSts="E": record-level update using <RcrdId> = CPSMS_CREDIT_TRAN
	 * (if present).
	 */
	private void processDataRecord(FileAck.FileAckDetails.MsgTp.Data data, AckFileType fileType,
			String ackFileNameFromMsgId) {

		// original PAY filename from Data.MsgId=""
		String originalFileName = data.getMsgId();

		// Batch number from InfId.Id=""
		FileAck.FileAckDetails.MsgTp.Data.InfId infId = data.getInfId();
		if (infId == null) {
			LOGGER.warn("Skipping <Data> without InfId. Data MsgId={}", originalFileName);
			return;
		}

		String batchNumber = infId.getId();

		// Group status & error code from <Data>
		String gpSts = data.getGpSts(); // "S" or "E"
		String errCd = normalizeEmptyToNull(data.getErrCd());

		// RecordId (CPSMS_CREDIT_TRAN) only present if GpSts="E"
		String recordId = null;
		FileAck.FileAckDetails.MsgTp.Data.InfId.Err errElement = infId.getErr();
		if (errElement != null) {
			recordId = normalizeEmptyToNull(errElement.getRcrdId());
		}

		boolean isErrorGroup = "E".equalsIgnoreCase(gpSts);
		LOGGER.info(
				"Processing Data record: batchNumber={}, originalFileName={}, fileType={}, GpSts={}, ErrCd={}, RcrdId={}",
				batchNumber, originalFileName, fileType, gpSts, errCd, recordId);

		// 1) Find batch via BATCH_NUMBER
		Optional<PaymentBatchMaster> batchOpt = batchRepository.findByBatchNumber(batchNumber);
		if (batchOpt.isEmpty()) {
			LOGGER.warn("No PAYMENT_BATCH_MASTER found for BATCH_NUMBER='{}'. Skipping this Data.", batchNumber);
			return;
		}

		PaymentBatchMaster batch = batchOpt.get();

		// 2) Find credits according to success/error + RcrdId availability
		List<PaymentCreditMaster> credits;
		if (isErrorGroup && recordId != null) {
			// Error-level ACK – use CPSMS_CREDIT_TRAN
			credits = findCreditsForError(fileType, batch, originalFileName, recordId);
		} else {
			// Success-level ACK (or missing RcrdId) – group update
			credits = findCreditsForGroup(fileType, batch, originalFileName);
		}

		if (credits.isEmpty()) {
			LOGGER.warn(
					"No PAYMENT_CREDIT_MASTER records found for batchNumber='{}', originalFileName='{}', "
							+ "fileType={}, isErrorGroup={}, recordId={}. Nothing to update.",
					batchNumber, originalFileName, fileType, isErrorGroup, recordId);
			return;
		}

		// 3) Apply update according to file type (INI/SUC/REJ)
		switch (fileType) {
		case INI -> applyIniAckUpdate(credits, ackFileNameFromMsgId, gpSts, errCd);
		case SUC -> applySucAckUpdate(credits, ackFileNameFromMsgId, gpSts, errCd);
		case REJ -> applyRejAckUpdate(credits, ackFileNameFromMsgId, gpSts, errCd);
		}

		creditRepository.saveAll(credits);

		LOGGER.info(
				"Updated {} PAYMENT_CREDIT_MASTER records for batchNumber='{}', originalFileName='{}', "
						+ "fileType={}, isErrorGroup={}, recordId={}",
				credits.size(), batchNumber, originalFileName, fileType, isErrorGroup, recordId);
	}

	/**
	 * Group-level lookup (no CPSMS_CREDIT_TRAN). Used when GpSts="S" OR when RcrdId
	 * is missing.
	 */
	private List<PaymentCreditMaster> findCreditsForGroup(AckFileType fileType, PaymentBatchMaster batch,
			String originalFileName) {

		return switch (fileType) {
		case INI -> creditRepository.findByBatchAndPaymentInitiReqId(batch, originalFileName);
		case SUC -> creditRepository.findByBatchAndPaymentSuccessReqId(batch, originalFileName);
		case REJ -> creditRepository.findByBatchAndFailureRequestId(batch, originalFileName);
		};
	}

	/**
	 * Record-level lookup using CPSMS_CREDIT_TRAN (RcrdId). Used when GpSts="E" AND
	 * RcrdId is present.
	 */
	private List<PaymentCreditMaster> findCreditsForError(AckFileType fileType, PaymentBatchMaster batch,
			String originalFileName, String recordId) {

		if (recordId == null) {
			return Collections.emptyList();
		}

		return switch (fileType) {
		case INI ->
			creditRepository.findByBatchAndPaymentInitiReqIdAndCpsmsCreditTran(batch, originalFileName, recordId);
		case SUC ->
			creditRepository.findByBatchAndPaymentSuccessReqIdAndCpsmsCreditTran(batch, originalFileName, recordId);
		case REJ ->
			creditRepository.findByBatchAndFailureRequestIdAndCpsmsCreditTran(batch, originalFileName, recordId);
		};
	}

	// ========= Apply ACK updates per file type =========

	private void applyIniAckUpdate(List<PaymentCreditMaster> credits, String ackFileName, String grpStatus,
			String errorCode) {

		// INI file: update only INI columns; SUC & REJ fields must be NULL (as per
		// requirement).
		for (PaymentCreditMaster pcm : credits) {
			pcm.setIniAckName(ackFileName);
			pcm.setIniGrpStatus(grpStatus);
			pcm.setIniAckErrorCode(errorCode);

			pcm.setSucAckName(null);
			pcm.setSucGrpStatus(null);
			pcm.setSucAckErrorCode(null);

			pcm.setRejAckName(null);
			pcm.setRejGrpStatus(null);
			pcm.setRejAckErrorCode(null);
		}
	}

	private void applySucAckUpdate(List<PaymentCreditMaster> credits, String ackFileName, String grpStatus,
			String errorCode) {

		// SUC file: update only SUC columns; INI & REJ fields must be NULL.
		for (PaymentCreditMaster pcm : credits) {
			pcm.setSucAckName(ackFileName);
			pcm.setSucGrpStatus(grpStatus);
			pcm.setSucAckErrorCode(errorCode);

			pcm.setIniAckName(null);
			pcm.setIniGrpStatus(null);
			pcm.setIniAckErrorCode(null);

			pcm.setRejAckName(null);
			pcm.setRejGrpStatus(null);
			pcm.setRejAckErrorCode(null);
		}
	}

	private void applyRejAckUpdate(List<PaymentCreditMaster> credits, String ackFileName, String grpStatus,
			String errorCode) {

		// REJ file: update only REJ columns; INI & SUC fields must be NULL.
		for (PaymentCreditMaster pcm : credits) {
			pcm.setRejAckName(ackFileName);
			pcm.setRejGrpStatus(grpStatus);
			pcm.setRejAckErrorCode(errorCode);

			pcm.setIniAckName(null);
			pcm.setIniGrpStatus(null);
			pcm.setIniAckErrorCode(null);

			pcm.setSucAckName(null);
			pcm.setSucGrpStatus(null);
			pcm.setSucAckErrorCode(null);
		}
	}

	private String normalizeEmptyToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
