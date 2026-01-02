package com.intech.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * External SQL configuration.
 *
 * The file pointed by sql.queries.path must be a standard Java .properties file
 * containing required query keys (ACCT_SELECT, BAL, CNT, TXN).
 */
@Configuration
//@ConfigurationProperties(prefix = "sql")
@ConfigurationProperties(prefix = "queries")
public class SqlProperties {

    /**
     * Absolute path to the external queries.properties file.
     */
	@Value("${queries.properties.path}")
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
