package com.intech.EXAckReader.service;

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.intech.EXAckReader.model.AckExceptionRecord;

/**
 * Processes all *.xml files from the input directory: 1) parse XML 2) update DB
 * 3) rename file to .xml.done (same folder)
 */
@Service
public class AckFileProcessor {
	private static final Logger log = LoggerFactory.getLogger(AckFileProcessor.class);

	private final AckXmlParser parser;
	private final TxnReportsDtlsDao dao;

	public AckFileProcessor(AckXmlParser parser, TxnReportsDtlsDao dao) {
		this.parser = parser;
		this.dao = dao;
	}

	public void processDirectoryOnce(Path inputDir) {
		if (inputDir == null)
			return;

		if (!Files.exists(inputDir) || !Files.isDirectory(inputDir)) {
			log.warn("ACK input directory not found / not a directory: {}", inputDir);
			return;
		}

		// Strictly pick only .xml
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDir, "*.xml")) {
			for (Path xmlFile : stream) {
				processOneFile(xmlFile);
			}
		} catch (Exception e) {
			log.error("Failed while scanning directory: {}", inputDir, e);
		}
	}

	private void processOneFile(Path xmlFile) {
		// Basic safety check
		if (xmlFile == null || !Files.isRegularFile(xmlFile))
			return;

		// Simple lock so two scheduler runs don’t process the same file simultaneously
		try (FileChannel channel = FileChannel.open(xmlFile, StandardOpenOption.READ);
				FileLock lock = channel.tryLock(0L, Long.MAX_VALUE, true)) {

			if (lock == null) {
				log.info("File is locked / in use, skipping: {}", xmlFile.getFileName());
				return;
			}

			log.info("Processing ACK file: {}", xmlFile.getFileName());

			List<AckExceptionRecord> records = parser.parse(xmlFile);

			int totalUpdated = 0;
			for (AckExceptionRecord r : records) {
				totalUpdated += dao.updateFromAck(r);
			}

			// Rename only after successful processing (parse + db calls completed)
			renameToDone(xmlFile);

			log.info("Completed file: {} | updatedRows={}", xmlFile.getFileName(), totalUpdated);

		} catch (Exception e) {
			// As per your instruction: do not rename on failure; just log error
			// (prod-style)
			log.error("Failed processing file: {}", xmlFile.getFileName(), e);
		}
	}

	private void renameToDone(Path xmlFile) throws Exception {
		Path done = xmlFile.resolveSibling(xmlFile.getFileName().toString() + ".done");
		Files.move(xmlFile, done, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
	}
}
