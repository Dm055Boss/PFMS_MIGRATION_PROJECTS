package com.intech.config;

import com.intech.exception.ConfigException;
import com.intech.sql.QueryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Validates production-critical configuration at startup.
 *
 * Fail-fast on:
 * - missing external queries file
 * - missing required query keys
 * - missing output directory (unless createDir=true and can be created)
 * - missing required txn.root and bank attributes
 * - missing scheduler cron if scheduler is enabled
 */
@Component
public class StartupValidator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupValidator.class);

    private final SqlProperties sqlProperties;
    private final TxnProperties txnProperties;
    private final SchedulerProperties schedulerProperties;
    private final QueryStore queryStore;

    public StartupValidator(SqlProperties sqlProperties,
                            TxnProperties txnProperties,
                            SchedulerProperties schedulerProperties,
                            QueryStore queryStore) {
        this.sqlProperties = sqlProperties;
        this.txnProperties = txnProperties;
        this.schedulerProperties = schedulerProperties;
        this.queryStore = queryStore;
    }

    @Override
    public void run(ApplicationArguments args) {
        validateQueriesFile();
        validateTxnBasics();
        validateOutputDir();
        validateScheduler();

        // Ensure required query keys exist at startup.
        queryStore.reloadIfNeeded(true);
        List<String> requiredKeys = List.of("ACCT_SELECT", "BAL", "CNT", "TXN");
        for (String key : requiredKeys) {
            if (queryStore.get(key).isBlank()) {
                throw new ConfigException("Missing required SQL key in queries file: " + key);
            }
        }

        log.info("Startup validation completed successfully. Scheduler enabled={}, cron='{}', outputDir='{}'",
                schedulerProperties.isEnabled(), schedulerProperties.getCron(), txnProperties.getOutput().getDir());
    }

    private void validateQueriesFile() {
        String path = sqlProperties.getQueriesPath();
        if (path == null || path.isBlank()) {
            throw new ConfigException("Missing required property: sql.queries.path");
        }
        Path p = Path.of(path);
        if (!Files.exists(p) || !Files.isRegularFile(p)) {
            throw new ConfigException("SQL queries file not found: " + p);
        }
    }

    private void validateTxnBasics() {
        if (txnProperties.getBank().getCode() == null || txnProperties.getBank().getCode().isBlank()) {
            throw new ConfigException("Missing required property: txn.bank.code");
        }
        if (txnProperties.getBank().getName() == null || txnProperties.getBank().getName().isBlank()) {
            throw new ConfigException("Missing required property: txn.bank.name");
        }
        if (txnProperties.getRoot().getSource() == null || txnProperties.getRoot().getSource().isBlank()) {
            throw new ConfigException("Missing required property: txn.root.source");
        }
        if (txnProperties.getOutput().getDir() == null || txnProperties.getOutput().getDir().isBlank()) {
            throw new ConfigException("Missing required property: txn.output.dir");
        }
    }

    private void validateOutputDir() {
        Path out = Path.of(txnProperties.getOutput().getDir());
        if (Files.exists(out)) {
            if (!Files.isDirectory(out)) {
                throw new ConfigException("txn.output.dir exists but is not a directory: " + out);
            }
            if (!Files.isWritable(out)) {
                throw new ConfigException("txn.output.dir is not writable: " + out);
            }
            return;
        }

        if (!txnProperties.getOutput().isCreateDir()) {
            throw new ConfigException("txn.output.dir does not exist and createDir=false: " + out);
        }

        try {
            Files.createDirectories(out);
            log.warn("Output directory did not exist; created: {}", out);
        } catch (Exception e) {
            throw new ConfigException("Failed to create txn.output.dir: " + out, e);
        }
    }

    private void validateScheduler() {
        if (!schedulerProperties.isEnabled()) {
            log.warn("Scheduler is disabled (scheduler.enabled=false). The application will not run automatically.");
            return;
        }
        if (schedulerProperties.getCron() == null || schedulerProperties.getCron().isBlank()) {
            throw new ConfigException("scheduler.enabled=true but scheduler.cron is missing/blank.");
        }
    }
}
