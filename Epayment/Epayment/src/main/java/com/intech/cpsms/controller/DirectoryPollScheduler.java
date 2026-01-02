// service/DirectoryPollScheduler.java
package com.intech.cpsms.controller;

import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.intech.cpsms.config.CpsmsProps;
import com.intech.cpsms.service.FileProcessingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "cpsms.scheduler", name = "enabled", havingValue = "true")
public class DirectoryPollScheduler {
	private static final Logger log = LogManager.getLogger(DirectoryPollScheduler.class);

    private final CpsmsProps props;                 // for req/res/err paths
    private final FileProcessingService processor;  // your existing service
    
    

    public DirectoryPollScheduler(CpsmsProps props, FileProcessingService processor) {
		super();
		this.props = props;
		this.processor = processor;
	}

	// Injected scheduler knobs (with safe defaults)
    @Value("${cpsms.scheduler.max-files-per-run:50}")
    private int maxFilesPerRun;

    @Value("${cpsms.scheduler.min-age-seconds:5}")
    private int minAgeSeconds;

    @Value("${cpsms.scheduler.file-glob:*.xml}")
    private String fileGlob;

    // default: ${java.io.tmpdir}/cpsms-cpsms.lock
    @Value("${cpsms.scheduler.lock-file:#{systemProperties['java.io.tmpdir'] + '/cpsms-cpsms.lock'}}")
    private String lockFilePath;

    // Runs on your cron (configure in application.properties)
    @Scheduled(cron = "${cpsms.scheduler.cron:0 */1 * * * *}")
    public void tick() {
        // single-instance lock (prevents concurrent schedulers on shared FS)
        try (RandomAccessFile raf = new RandomAccessFile(lockFilePath, "rw");
             FileChannel channel = raf.getChannel();
             FileLock lock = channel.tryLock()) {

            if (lock == null) {
                log.debug("Scheduler skipped: another instance holds the lock {}", lockFilePath);
                return;
            }

            int processed = runOneCycle();
            log.info("Scheduler cycle complete. Processed files: {}", processed);

        } catch (Exception e) {
            log.error("Scheduler cycle error", e);
        }
    }

    private int runOneCycle() throws Exception {
        String reqDirStr = props.getPaths().getReq();
        if (reqDirStr == null || reqDirStr.isBlank()) {
            log.warn("REQ path not configured; skipping cycle.");
            return 0;
        }

        Path reqDir = Paths.get(reqDirStr);
        if (!Files.isDirectory(reqDir)) {
            log.warn("REQ path does not exist or is not a directory: {}", reqDir);
            return 0;
        }

        Instant cutoff = Instant.now().minusSeconds(minAgeSeconds);

        try (Stream<Path> s = Files.list(reqDir)) {
            List<Path> files = s.filter(Files::isRegularFile)
                    .filter(p -> !p.getFileName().toString().endsWith(".done"))
                    .filter(p -> !p.getFileName().toString().startsWith("~"))
                    .filter(p -> matchesGlob(p.getFileName().toString(), fileGlob))
                    .filter(p -> olderThan(p, cutoff))
                    .sorted(Comparator.comparing(this::lastModifiedSafe)) // FIFO-ish
                    .limit(maxFilesPerRun)
                    .toList();

            if (files.isEmpty()) return 0;

            // If your FileProcessingService scans the dir itself, call that instead.
            // return processor.processAll();
            return processor.processAll(files);
        }
    }

    // ---------- helpers ----------
    private boolean matchesGlob(String name, String glob) {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
        return matcher.matches(Paths.get(name));
    }

    private boolean olderThan(Path p, Instant cutoff) {
        try {
            return Files.getLastModifiedTime(p).toInstant().isBefore(cutoff);
        } catch (Exception e) {
            return false;
        }
    }

    private long lastModifiedSafe(Path p) {
        try {
            return Files.getLastModifiedTime(p).toMillis();
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }
}
