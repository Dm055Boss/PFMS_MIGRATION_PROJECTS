// src/main/java/com/intech/epayackreader/scheduler/FileAckScheduler.java
package com.intech.EpayACKReaders.schedular;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.intech.EpayACKReaders.config.AckReaderProperties;
import com.intech.EpayACKReaders.service.FileAckProcessingService;

@Component
public class FileAckScheduler {

	private static final Logger LOGGER = LogManager.getLogger(FileAckScheduler.class);

	private final AckReaderProperties properties;
	private final FileAckProcessingService processingService;

	public FileAckScheduler(AckReaderProperties properties, FileAckProcessingService processingService) {
		this.properties = properties;
		this.processingService = processingService;
	}

	@Scheduled(cron = "#{@ackReaderProperties.cron}")
	public void pollAndProcessAckFiles() {
		if (!properties.isSchedulerEnabled()) {
			LOGGER.debug("ACK scheduler is disabled. Skipping this run.");
			return;
		}

		LOGGER.info("Starting ACK scheduler run...");

		List<Path> candidateFiles = collectCandidateFiles();

		if (candidateFiles.isEmpty()) {
			LOGGER.info("No ACK files (.xml) found to process.");
			return;
		}

		int processedCount = 0;
		for (Path path : candidateFiles) {
			if (processedCount >= properties.getMaxFilesPerRun()) {
				LOGGER.info("Reached max-files-per-run limit: {}. Remaining files will be processed in next run.",
						properties.getMaxFilesPerRun());
				break;
			}

			try {
				processingService.processFile(path);

				// SUCCESS: rename to .xml.done at same location
				renameWithSuffix(path, ".done");

				processedCount++;
			} catch (Exception ex) {
				LOGGER.error("Error while processing ACK file {}: {}", path, ex.getMessage(), ex);

				// ERROR: rename to .xml.error to avoid infinite retry loop
				renameWithSuffix(path, ".error");
			}
		}

		LOGGER.info("ACK scheduler run completed. Processed {} file(s).", processedCount);
	}

	/**
	 * Collects only .xml files older than minAgeSeconds from all input dirs.
	 */
	private List<Path> collectCandidateFiles() {
		List<Path> result = new ArrayList<>();
		System.out.println("properties.getInputDirs()  >>" + properties.getInputDirs());
		if (properties.getInputDirs() == null || properties.getInputDirs().isEmpty()) {
			LOGGER.warn("No input directories configured for ACK/NACK in properties file...");
			return result;
		}

//		int minAgeSeconds = properties.getMinAgeSeconds();
		String fileGlob = properties.getFileGlob(); // typically "*.xml"

		for (String dirStr : properties.getInputDirs()) {
			Path dir = Paths.get(dirStr);
			if (!Files.isDirectory(dir)) {
				LOGGER.warn("Input directory does not exist ......... ", dir);
				continue;
			}

			try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, fileGlob)) {

				for (Path path : stream) {
					if (!Files.isRegularFile(path)) {
						continue;
					}

					String fileName = path.getFileName().toString().toLowerCase();

					// Strict rule: process only .xml (NOT .xml.done, .xml.error, etc.)
					if (!fileName.endsWith(".xml") || fileName.endsWith(".xml.done")||fileName.endsWith(".done")) {
						LOGGER.info("Skipping non-XML file: {}", path);
						continue;
					}

					long lastModifiedMillis = Files.getLastModifiedTime(path).toMillis();
					long ageSeconds = (Instant.now().toEpochMilli() - lastModifiedMillis) / 1000;
					

					/*
					 * if (ageSeconds < minAgeSeconds) {
					 * LOGGER.debug("Skipping file {} as it is too new (age={} sec)", path,
					 * ageSeconds); continue; }
					 */

					result.add(path);
				}

			} catch (IOException e) {
				LOGGER.error("Error reading directory {}: {}", dir, e.getMessage(), e);
			}
		}

		// Process oldest files first
		result.sort(Comparator.comparingLong(p -> {
			try {
				return Files.getLastModifiedTime(p).toMillis();
			} catch (IOException e) {
				LOGGER.warn("Unable to get lastModifiedTime for {}: {}", p, e.getMessage());
				return 0L;
			}
		}));

		LOGGER.info("Found {} ACK .xml file(s) to consider in this run.", result.size());
		return result;
	}

	/**
	 * Rename foo.xml -> foo.xml.done or foo.xml.error in the same directory.
	 */
	private void renameWithSuffix(Path source, String suffix) {
		try {
			Path parent = source.getParent();
			String fileName = source.getFileName().toString();
			Path target = parent.resolve(fileName + suffix);

			Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
			LOGGER.info("Renamed file {} -> {}", source, target);
		} catch (IOException e) {
			LOGGER.error("Failed to rename file {} with suffix '{}': {}", source, suffix, e.getMessage(), e);
		}
	}
}
