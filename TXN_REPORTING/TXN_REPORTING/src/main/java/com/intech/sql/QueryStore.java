package com.intech.sql;

import com.intech.config.SqlProperties;
import com.intech.exception.ConfigException;
import com.intech.exception.QueryNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

/**
 * Loads SQL queries from an external .properties file.
 *
 * The file is expected to contain keys:
 * - ACCT_SELECT
 * - BAL
 * - CNT
 * - TXN
 *
 * Production behavior:
 * - supports hot-fix updates by reloading each run when sql.reload-each-run=true
 */
@Component
public class QueryStore {

    private static final Logger log = LoggerFactory.getLogger(QueryStore.class);

    private final SqlProperties sqlProperties;

    private volatile Properties cached = new Properties();
    private volatile long lastModified = -1;

    public QueryStore(SqlProperties sqlProperties) {
        this.sqlProperties = sqlProperties;
    }

    /**
     * Reloads the queries file if needed.
     *
     * @param force true to force reload even if unchanged
     */
    public synchronized void reloadIfNeeded(boolean force) {
        Path p = Path.of(sqlProperties.getQueriesPath());
        long lm;
        try {
            lm = Files.getLastModifiedTime(p).toMillis();
        } catch (Exception e) {
            throw new ConfigException("Unable to read last-modified time for queries file: " + p, e);
        }

        if (!force && !sqlProperties.isReloadEachRun() && lastModified == lm) {
            return;
        }

        if (!force && sqlProperties.isReloadEachRun() && lastModified == lm) {
            // Reload-each-run is enabled but file hasn't changed; keep cache.
            return;
        }

        Properties props = new Properties();
        try (InputStream is = Files.newInputStream(p)) {
            props.load(is);
        } catch (Exception e) {
            throw new ConfigException("Failed to read queries file: " + p, e);
        }

        cached = props;
        lastModified = lm;
        log.info("Loaded SQL queries from {} (lastModified={})", p, lm);
    }

    /**
     * Returns a SQL string for a given key.
     *
     * @throws QueryNotFoundException if key is missing or blank
     */
    public String get(String key) {
        Objects.requireNonNull(key, "key");
        String sql = cached.getProperty(key);
        if (sql == null || sql.isBlank()) {
            throw new QueryNotFoundException("SQL not found for key: " + key);
        }
        return sql.trim();
    }
}
