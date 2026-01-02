package com.intech.EpayRejFileGenerator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "reject")
public class RejectAckProperties {

	// Scheduler
	private String schedulerCron;

	// Business constants
	private String bankCode;
	private String bankName;
	private String destination;
	private String source;
	private String outputDir;

	// Counter file (PAYINICNTR.properties)
	private String counterFile; // /pfmsapp/CPSMS/ManualGenerator/ANYM/REG/PAYINICNTR.properties
	private String counterKey; // e.g. PAYREJ
	private int counterStart; // e.g. 700000

	// SQLs
	private String sqlSelectBatches;
	private String sqlSelectCreditsByBatch;
	private String sqlInsertAudit;
    private String sqlUpdateFailureRequestId;

	// getters & setters

	public String getSchedulerCron() {
		return schedulerCron;
	}

	public void setSchedulerCron(String schedulerCron) {
		this.schedulerCron = schedulerCron;
	}

	public String getBankCode() {
		return bankCode;
	}

	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	public String getCounterFile() {
		return counterFile;
	}

	public void setCounterFile(String counterFile) {
		this.counterFile = counterFile;
	}

	public String getCounterKey() {
		return counterKey;
	}

	public void setCounterKey(String counterKey) {
		this.counterKey = counterKey;
	}

	public int getCounterStart() {
		return counterStart;
	}

	public void setCounterStart(int counterStart) {
		this.counterStart = counterStart;
	}

	public String getSqlSelectBatches() {
		return sqlSelectBatches;
	}

	public void setSqlSelectBatches(String sqlSelectBatches) {
		this.sqlSelectBatches = sqlSelectBatches;
	}

	public String getSqlSelectCreditsByBatch() {
		return sqlSelectCreditsByBatch;
	}

	public void setSqlSelectCreditsByBatch(String sqlSelectCreditsByBatch) {
		this.sqlSelectCreditsByBatch = sqlSelectCreditsByBatch;
	}

	public String getSqlInsertAudit() {
		return sqlInsertAudit;
	}

	public void setSqlInsertAudit(String sqlInsertAudit) {
		this.sqlInsertAudit = sqlInsertAudit;
	}

	public String getSqlUpdateFailureRequestId() {
		return sqlUpdateFailureRequestId;
	}

	public void setSqlUpdateFailureRequestId(String sqlUpdateFailureRequestId) {
		this.sqlUpdateFailureRequestId = sqlUpdateFailureRequestId;
	}
	
}
