package com.intech.domain;

import java.time.Duration;

/**
 * Summary of one job execution run.
 */
public class JobSummary {

    private int selectedAccounts;
    private int processedAccounts;
    private int skippedAccounts;
    private int failedAccounts;
    private Duration duration = Duration.ZERO;
    private String outputFile;

    public int getSelectedAccounts() { return selectedAccounts; }

    public void setSelectedAccounts(int selectedAccounts) { this.selectedAccounts = selectedAccounts; }

    public int getProcessedAccounts() { return processedAccounts; }

    public void setProcessedAccounts(int processedAccounts) { this.processedAccounts = processedAccounts; }

    public int getSkippedAccounts() { return skippedAccounts; }

    public void setSkippedAccounts(int skippedAccounts) { this.skippedAccounts = skippedAccounts; }

    public int getFailedAccounts() { return failedAccounts; }

    public void setFailedAccounts(int failedAccounts) { this.failedAccounts = failedAccounts; }

    public Duration getDuration() { return duration; }

    public void setDuration(Duration duration) { this.duration = duration; }

    public String getOutputFile() { return outputFile; }

    public void setOutputFile(String outputFile) { this.outputFile = outputFile; }
}
