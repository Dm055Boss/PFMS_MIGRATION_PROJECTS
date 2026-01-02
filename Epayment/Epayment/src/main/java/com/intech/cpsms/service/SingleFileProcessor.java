package com.intech.cpsms.service;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intech.cpsms.config.CpsmsProps;
import com.intech.cpsms.dto.DscResult;
import com.intech.cpsms.dto.ParsedBatchDTO;
import com.intech.cpsms.util.FilenameRules;

@Service
public class SingleFileProcessor {

	private final XmlParserService xmlParser;
	private final DscValidatorAdapter dscValidator;
	private final PersistenceService persistence;
	private final ResponseBuilderService responseBuilder;
	private final DuplicateGuardService duplicateGuard;
	private final CpsmsProps props;

	public SingleFileProcessor(XmlParserService xmlParser, DscValidatorAdapter dscValidator,
			PersistenceService persistence, ResponseBuilderService responseBuilder,
			DuplicateGuardService duplicateGuard, CpsmsProps props) {
		this.xmlParser = xmlParser;
		this.dscValidator = dscValidator;
		this.persistence = persistence;
		this.responseBuilder = responseBuilder;
		this.duplicateGuard = duplicateGuard;
		this.props = props;
	}

	/**
	 * Rules: 1) If READ/PARSE fails -> move request to ErrorFiles (as-is), NO
	 * response. Return false. 2) If READ succeeds -> always generate response
	 * (ACK/NACK), then rename request to "<file>.done". Return true. 3) If
	 * (RequestMessageId, BatchNumber) already exists in PAYMENT_BATCH_MASTER: - Do
	 * NOT insert into any master tables - Generate NACK (duplicate) using existing
	 * response builder - Rename request to .done
	 */
	public boolean processOneSafely(Path reqFile) {
		ParsedBatchDTO parsed;

		// --- 1) READ & PARSE ---
		try {
			parsed = xmlParser.parse(reqFile); // if this fails -> ErrorFiles
		} catch (Exception readEx) {
			moveToDirSilently(reqFile, props.getPaths().getErr());
			return false;
		}

		try {
			// --- 2) PREPARE RESPONSE MESSAGE ID (ACK/NACK file name) ---
			String responseMsgId = FilenameRules.responseMessageIdFromRequest(parsed.getRequestMessageId());
			parsed.setResponseMessageId(responseMsgId);

			// --- 3) DSC DECISION (keep legacy behavior) ---
			// PaymentProduct is already upper-cased in parser
			String prod = parsed.getPaymentProduct();
			if (prod == null) {
				prod = "";
			}

			switch (prod) {
			case "EPA":
			case "DSC":
			case "CDDO":
			case "PAO":
				if (parsed.isSignaturePresent()) {
					// Uses existing legacy classes internally
					String res = dscValidator.validate(reqFile);
					parsed.setDscResult(res); // setter maps to DscResult enum
				} else {
					// Signature required but not found
					parsed.setDscResult("DSCTAGNOTFOUND");
				}
				break;

			case "PPA":
				// No signature required
				parsed.setDscResult(DscResult.NOT_APPLICABLE);
				break;

			default:
				// Future-safe: treat as no DSC requirement unless you define otherwise
				parsed.setDscResult(DscResult.NOT_APPLICABLE);
				break;
			}

			// --- 4) DUPLICATE CHECK (xBatchQuery-style) ---
			// Uses PAYMENT_BATCH_MASTER count to detect if this (reqMsgId, batchNo) already
			// processed
			boolean isDuplicate = duplicateGuard.markIfDuplicate(parsed);
			if (isDuplicate) {
				// Important:
				// - DO NOT persist anything
				// - ResponseBuilder will see formatValid=false + DUPLICATE error
				// and generate simple NACK (no transactions)
				responseBuilder.generateAndWrite(parsed);
				renameToDone(reqFile);
				return true;
			}

			// --- 5) PERSIST (only for NON-DUPLICATE) ---
			persistTransactional(parsed);

			// --- 6) RESPONSE (ACK/NACK based on DSC + format + content) ---
			responseBuilder.generateAndWrite(parsed);

			// --- 7) RENAME request to .done ---
			renameToDone(reqFile);
			return true;

		} catch (Exception exAfterRead) {
			// Read succeeded, but something later failed.
			// Per your rule: do NOT move to ErrorFiles now.
			// Best-effort rename to .done so operations can reconcile.
			try {
				renameToDone(reqFile);
			} catch (Exception ignore) {
				// swallow
			}
			return true;
		}
	}

	@Transactional
	protected void persistTransactional(ParsedBatchDTO parsed) {
		persistence.persist(parsed);
	}

	private void renameToDone(Path source) throws Exception {
		Path target = source.resolveSibling(source.getFileName().toString() + ".done");
		Files.move(source, target, REPLACE_EXISTING);
	}

	private void moveToDirSilently(Path source, String targetDirStr) {
		try {
			if (targetDirStr == null || targetDirStr.isBlank()) {
				return;
			}
			Path targetDir = Paths.get(targetDirStr);
			Files.createDirectories(targetDir);
			Path target = targetDir.resolve(source.getFileName());
			Files.move(source, target, REPLACE_EXISTING);
		} catch (Exception ignore) {
			// swallow per requirement
		}
	}
}
