package com.intech.cpsms.service;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.intech.cpsms.config.CpsmsProps;

@Service
public class FileProcessingService {

	private static final Logger log = LoggerFactory.getLogger(FileProcessingService.class);

	private final SingleFileProcessor singleFileProcessor;
	private final CpsmsProps props;

	public FileProcessingService(SingleFileProcessor singleFileProcessor, CpsmsProps props) {
		this.singleFileProcessor = singleFileProcessor;
		this.props = props;
	}

	public int processAll() {
		String reqDirStr = props.getPaths().getReq();
		String cfgPattern = props.getFile().getPattern(); // may be null/blank

		if (reqDirStr == null || reqDirStr.isBlank()) {
			log.error("cpsms.paths.req is null/blank");
			return 0;
		}

		// Make it effectively final for lambdas
		final String pattern = (cfgPattern == null || cfgPattern.isBlank()) ? "*.xml" : cfgPattern;
		final Path reqDir = Paths.get(reqDirStr);

		if (!Files.isDirectory(reqDir)) {
			log.warn("REQ directory not found or not a directory: {}", reqDir.toAbsolutePath());
			return 0;
		}

		final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
		final AtomicInteger count = new AtomicInteger(0);

		try (Stream<Path> files = Files.list(reqDir)) {
			files.filter(p -> !Files.isDirectory(p)).peek(p -> log.debug("Found file: {}", p.getFileName()))
					.filter(p -> {
						boolean match = matcher.matches(p.getFileName());
						if (!match) {
							log.debug("Skipping (pattern mismatch): {} (glob: {})", p.getFileName(), pattern);
						}
						return match;
					}).forEach(p -> {
						try {
							boolean ok = singleFileProcessor.processOneSafely(p);
							if (ok)
								count.incrementAndGet();
						} catch (Exception e) {
							log.error("Failed processing {}: {}", p.getFileName(), e.toString());
						}
					});
		} catch (Exception e) {
			log.error("Failed to scan REQ directory {}: {}", reqDir, e.toString());
			return count.get();
		}

		log.info("Processed files (read OK): {}", count.get());
		return count.get();
	}
	public int processAll(List<Path> files) {
        final int[] ok = {0};
        files.forEach(p -> {
            try {
                if (singleFileProcessor.processOneSafely(p)) ok[0]++;
            } catch (Exception e) {
                // already handled inside SingleFileProcessor; keep defensive
            }
        });
        return ok[0];
    }

}
