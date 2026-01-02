package com.idbi.cpsms.succack.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.idbi.cpsms.succack.bean.SuccessPayments;
import com.idbi.cpsms.succack.repository.SuccessAckJdbcRepository;
import com.idbi.cpsms.succack.service.dto.BatchInfo;
import com.idbi.cpsms.succack.service.dto.CreditInfo;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;

/**
 * Main scheduled job: generates Success Payment XML files. - Fetches eligible
 * batches/credits from DB - Builds JAXB SuccessPayments object - Writes XML
 * file to configured output dir - Updates PAYMENT_SUCCESS_REQ_ID - Inserts
 * audit row(s)
 */
@Service
public class SuccessAckJobService {

	private static final Logger log = LogManager.getLogger(SuccessAckJobService.class);
	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("ddMMyyyy");

	private final SuccessAckJdbcRepository repo;
	private final SuccessAckXmlMapper xmlMapper;
	private final MessageIdCounterService counterService;
	private final Path outputDir;
	private final AtomicBoolean running = new AtomicBoolean(false);

	public SuccessAckJobService(SuccessAckJdbcRepository repo, SuccessAckXmlMapper xmlMapper,
			MessageIdCounterService counterService,@Qualifier("successAckProperties") Properties successAckProperties) {

		this.repo = repo;
		this.xmlMapper = xmlMapper;
		this.counterService = counterService;
		this.outputDir = Paths.get(successAckProperties.getProperty("success-ack.output-dir"));
	}

	@Scheduled(cron = "${success-ack.job.cron}")
	public void runJob() {
		if (!running.compareAndSet(false, true)) {
			log.info("Success Ack job already running. Skipping this trigger.");
			return;
		}

		log.info("===== Success Ack generation job started =====");
		try {
			ensureOutputDir();

			List<BatchInfo> batches = repo.findEligibleBatches();
			if (batches.isEmpty()) {
				log.info("No eligible batches found for Success Ack file generation.");
				return;
			}

			for (BatchInfo batch : batches) {
				processSingleBatch(batch);
			}

		} catch (Exception e) {
			log.error("Unexpected error in Success Ack job", e);
		} finally {
			running.set(false);
			log.info("===== Success Ack generation job finished =====");
		}
	}

	private void processSingleBatch(BatchInfo batch) {
		log.info("Processing batch: id={}, number={}", batch.getBatchId(), batch.getBatchNumber());

		List<CreditInfo> credits = repo.findCreditsForBatch(batch.getBatchId());
		if (credits.isEmpty()) {
			log.warn("No eligible credits found for batch {}. Skipping.", batch.getBatchNumber());
			return;
		}

		String today = LocalDate.now().format(DATE_FMT);
		String serial = counterService.nextSerialForToday();
		String bankCode = batch.getBankCode();

		// 691 + EPA + SUCPAY + ddMMyyyy + serial
		String msgId = bankCode + batch.getPaymentProd() + "SUCPAY" + today + serial;

		try {
			// 1) Build JAXB object
			SuccessPayments xmlModel = xmlMapper.buildXml(batch, credits, msgId);

			// 2) Marshall to XML file
			Path out = outputDir.resolve(msgId + ".xml");
			marshalToFile(xmlModel, out.toFile());
			log.info("Generated Success Ack XML file: {}", out);

			// 3) Update credits with PAYMENT_SUCCESS_REQ_ID
			List<String> creditTrans = credits.stream().map(CreditInfo::getCpsmsCreditTran).toList();
			int updated = repo.updateCreditsWithMsgId(creditTrans, msgId);
			log.info("Updated PAYMENT_SUCCESS_REQ_ID for {} records in batch {}", updated, batch.getBatchNumber());

			// 4) Insert audit log
			writeAudit(batch, msgId);

		} catch (Exception e) {
			log.error("Error while generating Success Ack for batch {} (msgId={})", batch.getBatchNumber(), msgId, e);
			// If you want old behaviour (decrease counter on error), we can add that logic
			// here.
		}
	}

	private void marshalToFile(SuccessPayments xmlModel, File file) throws Exception {
		JAXBContext ctx = JAXBContext.newInstance(SuccessPayments.class);
		Marshaller marshaller = ctx.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(xmlModel, file);
	}

	private void ensureOutputDir() {
		try {
			if (!Files.exists(outputDir)) {
				Files.createDirectories(outputDir);
			}
		} catch (Exception e) {
			throw new IllegalStateException("Unable to create output dir: " + outputDir, e);
		}
	}

	private void writeAudit(BatchInfo batch, String msgId) {
		try {
			String prefix;
			if (msgId != null && msgId.length() >= 20) {
				prefix = msgId.substring(0, 20);
			} else {
				prefix = msgId;
			}

			int existing = repo.countAuditRecords(batch.getBatchNumber(), prefix);
			String debitStatus = repo.findDebitStatusForBatch(batch.getBatchId());
			if (debitStatus == null) {
				debitStatus = "PI";
			}

			if (existing == 0) {
				int rows = repo.insertFirstAudit(msgId, batch.getBatchNumber(), debitStatus);
				log.info("Inserted FIRST audit row for batch {} (rows={})", batch.getBatchNumber(), rows);
			} else {
				int attempt = existing + 1;
				int rows = repo.insertResendAudit(msgId, batch.getBatchNumber(), debitStatus, attempt);
				log.info("Inserted RESEND audit row for batch {} attempt {} (rows={})", batch.getBatchNumber(), attempt,
						rows);
			}

		} catch (Exception e) {
			// Don't fail entire job due to audit error
			log.error("Failed to write audit record for batch {} and msgId {}", batch.getBatchNumber(), msgId, e);
		}
	}
}
