package com.intech.service;

import com.intech.config.SqlProperties;
import com.intech.domain.*;
import com.intech.exception.DataFetchException;
import com.intech.sql.QueryStore;
import com.intech.util.DateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Orchestrates the end-to-end TXN job run.
 *
 * Flow (exactly per your requirement): 1) Pick accounts from TXN_ACCOUNT_STATUS
 * where DATA_REQ_FLAG='Y' 2) For each account: 2.1) Load account tag attributes
 * using BAL query 2.2) Load transaction count using CNT query 2.3) If count >
 * 0, load transaction rows using TXN query (and enrich DrCr via XCNT/KBDT) 3)
 * Generate one TXN XML file for the run 4) After successful file creation,
 * upsert per-account rows into TXN_REPORTS_DTLS
 */
@Service
public class TxnJobService {

	private static final Logger log = LoggerFactory.getLogger(TxnJobService.class);

	/**
	 * Prevents overlapping runs inside the same JVM. (If you later run multiple
	 * instances, use a DB/Redis lock.)
	 */
	private final ReentrantLock runLock = new ReentrantLock();

	private final SqlProperties sqlProperties;
	private final QueryStore queryStore;

	private final DateService dateService;
	private final AccountSelectionService accountSelectionService;
	private final BalanceService balanceService;
	private final TransactionService transactionService;
	private final TxnXmlWriter txnXmlWriter;

	// NEW: inserts/upserts entries after successful file creation
	private final TxnReportsDtlsService txnReportsDtlsService;

	public TxnJobService(SqlProperties sqlProperties, QueryStore queryStore, DateService dateService,
			AccountSelectionService accountSelectionService, BalanceService balanceService,
			TransactionService transactionService, TxnXmlWriter txnXmlWriter,
			TxnReportsDtlsService txnReportsDtlsService) {
		this.sqlProperties = sqlProperties;
		this.queryStore = queryStore;
		this.dateService = dateService;
		this.accountSelectionService = accountSelectionService;
		this.balanceService = balanceService;
		this.transactionService = transactionService;
		this.txnXmlWriter = txnXmlWriter;
		this.txnReportsDtlsService = txnReportsDtlsService;
	}

	/**
	 * Runs the job once.
	 *
	 * Production handling: - fail-fast if SQL file cannot be loaded - per-account
	 * errors do not kill the whole run (they are logged and skipped) - XML is
	 * written to temp file first and then atomically moved to final file name -
	 * after XML creation succeeds, we persist the run ledger in TXN_REPORTS_DTLS
	 */
	public JobSummary runJob() {
		// Avoid overlapping runs when scheduler triggers again before previous run
		// completes.
		if (!runLock.tryLock()) {
			log.warn("JOB_SKIPPED another run is already in progress");
			return new JobSummary();
		}

		Instant start = Instant.now();

		// Load or reload SQL queries (supports hot-fixes).
		queryStore.reloadIfNeeded(true);

		TxnRunContext ctx = dateService.buildRunContext();
		MDC.put("runId", ctx.getRunId());
		MDC.put("txnDate", ctx.getTxnDateDdMmYyyy());

		JobSummary summary = new JobSummary();
		Path outputFile = null;

		try {
			log.info("JOB_START messageId={}, txnDate={}, queriesPath={}", ctx.getMessageId(), ctx.getTxnDateDdMmYyyy(),
					sqlProperties.getQueriesPath());

			// Step 1: select candidates (TXN_ID + ACCT_NO)
			List<TxnAccountCandidate> candidates = accountSelectionService.selectAccounts();
			summary.setSelectedAccounts(candidates.size());
			log.info("ACCOUNTS_SELECTED count={}", candidates.size());

			List<AccountReport> accountReports = new ArrayList<>(candidates.size());

			// NEW: track accounts that completed successfully (for insertion into
			// TXN_REPORTS_DTLS)
			List<TxnAccountCandidate> processedCandidates = new ArrayList<>(candidates.size());

			for (TxnAccountCandidate cand : candidates) {
				if (cand == null || cand.getAccountNo() == null || cand.getAccountNo().isBlank()
						|| cand.getTxnId() == null) {
					summary.setSkippedAccounts(summary.getSkippedAccounts() + 1);
					continue;
				}

				final String acct = cand.getAccountNo().trim();
				MDC.put("accountNo", acct);
				MDC.put("txnId", String.valueOf(cand.getTxnId()));

				Instant accStart = Instant.now();

				try {
					// Step 2: Balance/Account attributes
					var balOpt = balanceService.loadBalance(acct, ctx.getTxnDateDdMmYyyy());
					if (balOpt.isEmpty()) {
						summary.setSkippedAccounts(summary.getSkippedAccounts() + 1);
						log.warn("BAL_MISSING account skipped (no GAM/SOL row found)");
						continue;
					}

					BalanceInfo bal = balOpt.get();

					// Step 3: Count
					int txnCount = transactionService.loadTransactionCount(acct, ctx.getTxnDateDdMmYyyy());
					log.info("CNT_OK txnCount={}", txnCount);

					AccountReport ar = new AccountReport(bal, txnCount);

					// Step 4: Details only if count > 0
					if (txnCount > 0) {
						List<TransactionDetail> txns = transactionService.loadTransactions(acct,
								ctx.getTxnDateDdMmYyyy());
						ar.getTransactions().addAll(txns);
						log.info("TXN_OK rows={}", txns.size());

						// Optional: keep TransactionCount consistent with fetched rows
						// If you have a setter, uncomment:
						// ar.setTransactionCount(txns.size());
					}

					accountReports.add(ar);
					processedCandidates.add(cand); // NEW: mark account as successfully processed

					summary.setProcessedAccounts(summary.getProcessedAccounts() + 1);
					log.info("ACCOUNT_DONE status=SUCCESS ms={}", Duration.between(accStart, Instant.now()).toMillis());

				} catch (DataFetchException e) {
					summary.setFailedAccounts(summary.getFailedAccounts() + 1);
					log.error("ACCOUNT_FAILED due to DB error: {}", e.getMessage(), e);
				} catch (Exception e) {
					summary.setFailedAccounts(summary.getFailedAccounts() + 1);
					log.error("ACCOUNT_FAILED unexpected error: {}", e.getMessage(), e);
				} finally {
					MDC.remove("accountNo");
					MDC.remove("txnId");
				}
			}

			// Step 5: Generate XML only for successfully processed accounts.
			outputFile = txnXmlWriter.write(ctx, accountReports);
			summary.setOutputFile(outputFile.toAbsolutePath().toString());

			// Step 6: After successful file creation, upsert ledger rows in
			// TXN_REPORTS_DTLS
			// - TXNREQFN is the generated request file name (MessageId.xml)
			// - STATUS can be "SUCCESS" / "GENERATED" as per your convention
			txnReportsDtlsService.upsertAfterFileCreation(processedCandidates, outputFile.getFileName().toString(),
					"SUCCESS");

			summary.setDuration(Duration.between(start, Instant.now()));

			log.info("JOB_SUMMARY selected={}, processed={}, skipped={}, failed={}, durationMs={}, outputFile={}",
					summary.getSelectedAccounts(), summary.getProcessedAccounts(), summary.getSkippedAccounts(),
					summary.getFailedAccounts(), summary.getDuration().toMillis(), summary.getOutputFile());

			return summary;

		} finally {
			try {
				MDC.clear();
			} finally {
				runLock.unlock();
			}
		}
	}
}
