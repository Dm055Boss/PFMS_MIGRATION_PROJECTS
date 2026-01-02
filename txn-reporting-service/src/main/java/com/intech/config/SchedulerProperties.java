
package com.intech.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Scheduler properties.
 */
@ConfigurationProperties(prefix = "scheduler")
public class SchedulerProperties {

    /**
     * Enable/disable scheduled runs.
     */
    private boolean enabled = true;

    /**
     * Cron expression for job execution.
     */
    private String cron;

    /**
     * Scheduler thread pool size.
     */
    private int poolSize = 1;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }
}
