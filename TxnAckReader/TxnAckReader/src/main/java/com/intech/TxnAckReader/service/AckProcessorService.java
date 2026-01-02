package com.intech.TxnAckReader.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.intech.TxnAckReader.dao.TxnReportsUpdater;
import com.intech.TxnAckReader.service.AckXmlParser.AckData;

@Service
public class AckProcessorService {

    private static final Logger log = LoggerFactory.getLogger(AckProcessorService.class);

    private final AckXmlParser parser;
    private final TxnReportsUpdater updater;

    @Value("${ack.pick.path}")
    private String pickPath;

    @Value("${min.file.age.seconds:0}")
    private long minFileAgeSeconds;

    public AckProcessorService(AckXmlParser parser, TxnReportsUpdater updater) {
        this.parser = parser;
        this.updater = updater;
    }

    public void processAckFolder() {
        Path dir = Paths.get(pickPath);

        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (Exception e) {
            log.error("Unable to create/access folder: {}", dir, e);
            return;
        }

        try (Stream<Path> files = Files.list(dir)) {
            files.filter(Files::isRegularFile)
                 // STRICT: pick ONLY .xml (so .xml.done is never picked)
                 .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".xml"))
                 .filter(this::isOldEnough)
                 .forEach(this::processOneFile);
        } catch (Exception e) {
            log.error("Failed to scan folder: {}", dir, e);
        }
    }

    private void processOneFile(Path xmlFile) {
        String fileName = xmlFile.getFileName().toString();
        log.info("Processing file: {}", fileName);

        try {
            AckData ack = parser.parse(xmlFile);

            int updated = updater.updateTxnReports(ack);
            log.info("DB update count={} for TXNREQFN={}", updated, ack.txnReqFn);

        } catch (Exception e) {
            log.error("Processing failed for file: {}", fileName, e);
        } finally {
            renameToDone(xmlFile);
        }
    }

    private void renameToDone(Path xmlFile) {
        try {
            Path doneFile = xmlFile.resolveSibling(xmlFile.getFileName().toString() + ".done"); // file.xml.done
            Files.move(xmlFile, doneFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("Renamed to: {}", doneFile.getFileName());
        } catch (Exception e) {
            log.error("Rename failed for file: {}", xmlFile.getFileName(), e);
        }
    }

    private boolean isOldEnough(Path p) {
        if (minFileAgeSeconds <= 0) return true;
        try {
            Instant lm = Files.getLastModifiedTime(p).toInstant();
            return lm.isBefore(Instant.now().minusSeconds(minFileAgeSeconds));
        } catch (Exception e) {
            // safer: skip if unsure
            return false;
        }
    }
}
