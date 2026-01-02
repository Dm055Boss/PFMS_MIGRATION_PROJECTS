package com.intech.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * External SQL configuration.
 *
 * The file pointed by sql.queries.path must be a standard Java .properties file
 * containing required query keys (ACCT_SELECT, BAL, CNT, TXN).
 */
@ConfigurationProperties(prefix = "sql")
public class SqlProperties {

    /**
     * Absolute path to the external queries.properties file.
     */
    private String queriesPath;

    /**
     * If true, re-read the queries file at the start of every run (safe for production hot-fixes).
     */
    private boolean reloadEachRun = true;

    public String getQueriesPath() {
        return queriesPath;
    }

    public void setQueriesPath(String queriesPath) {
        this.queriesPath = queriesPath;
    }

    public boolean isReloadEachRun() {
        return reloadEachRun;
    }

    public void setReloadEachRun(boolean reloadEachRun) {
        this.reloadEachRun = reloadEachRun;
    }
}
