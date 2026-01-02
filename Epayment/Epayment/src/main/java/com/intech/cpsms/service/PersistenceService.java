package com.intech.cpsms.service;

import java.util.Date;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intech.cpsms.domain.entity.PaymentBatchMaster;
import com.intech.cpsms.domain.entity.PaymentCreditMaster;
import com.intech.cpsms.domain.entity.PaymentDebitMaster;
import com.intech.cpsms.domain.repo.PaymentBatchMasterRepo;
import com.intech.cpsms.domain.repo.PaymentCreditMasterRepo;
import com.intech.cpsms.domain.repo.PaymentDebitMasterRepo;
import com.intech.cpsms.dto.DscResult;
import com.intech.cpsms.dto.ParsedBatchDTO;
import com.intech.cpsms.dto.ParsedCreditDTO;
import com.intech.cpsms.dto.ParsedDebitDTO;

@Service
public class PersistenceService {

	private final PaymentBatchMasterRepo batchRepo;
	private final PaymentDebitMasterRepo debitRepo;
	private final PaymentCreditMasterRepo creditRepo;
	private final StanService stanService;

	public PersistenceService(PaymentBatchMasterRepo batchRepo, PaymentDebitMasterRepo debitRepo,
			PaymentCreditMasterRepo creditRepo, StanService stanService) {
		this.batchRepo = batchRepo;
		this.debitRepo = debitRepo;
		this.creditRepo = creditRepo;
		this.stanService = stanService;
	}

	@Transactional
	public void persist(ParsedBatchDTO parsed) {
		// 1) Batch: insert (we already ensured "not duplicate" before calling)
		PaymentBatchMaster batch = saveBatch(parsed);

		// 2) Debits & Credits
		if (parsed.getDebits() == null) {
			return;
		}

		for (ParsedDebitDTO dDto : parsed.getDebits()) {
			// natural key for debit: (batch.id, cpsmsDebitTranId)
			String cpsmsDebitTranId = trimOrNull(dDto.getCpsmsDebitTranId());

			Optional<PaymentDebitMaster> existingDebit = (cpsmsDebitTranId == null) ? Optional.empty()
					: debitRepo.findByBatch_IdAndCpsmsDebitTranId(batch.getId(), cpsmsDebitTranId);

			PaymentDebitMaster debit = existingDebit.orElseGet(() -> {
				PaymentDebitMaster e = mapDebit(dDto, parsed);
				e.setBatch(batch);

				// STAN: generate if missing
				if (isBlank(e.getDebitStan())) {
					e.setDebitStan(stanService.newDebitStan());
				}

				return debitRepo.save(e);
			});

			// credits under this debit
			if (dDto.getCredits() != null) {
				for (ParsedCreditDTO cDto : dDto.getCredits()) {
					String cpsmsCreditTran = trimOrNull(cDto.getCpsmsCreditTran());

					Optional<PaymentCreditMaster> existingCredit = (cpsmsCreditTran == null) ? Optional.empty()
							: creditRepo.findByDebit_IdAndCpsmsCreditTran(debit.getId(), cpsmsCreditTran);

					existingCredit.orElseGet(() -> {
						PaymentCreditMaster c = mapCredit(cDto, parsed);
						c.setBatch(batch);
						c.setDebit(debit);

						if (isBlank(c.getCreditStan())) {
							c.setCreditStan(stanService.newCreditStan());
						}

						return creditRepo.save(c);
					});
				}
			}
		}
	}

	private PaymentBatchMaster saveBatch(ParsedBatchDTO p) {
		PaymentBatchMaster b = mapBatch(p);
		try {
			return batchRepo.save(b);
		} catch (DataIntegrityViolationException ex) {
			// In case of race, reload (but normally DuplicateGuardService avoids this)
			long count = batchRepo.countByRequestMessageIdAndBatchNumber(p.getRequestMessageId(), p.getBatchNumber());
			if (count > 0) {
				throw ex; // or load & return existing if you add a finder
			}
			throw ex;
		}
	}

	// ---------- mapping helpers (use your existing ones, trimmed) ----------

	private PaymentBatchMaster mapBatch(ParsedBatchDTO p) {
		PaymentBatchMaster b = new PaymentBatchMaster();
		b.setCorporateId(p.getCorporateId());
		b.setBatchNumber(clip(p.getBatchNumber(), 16));
		b.setBatchTime(p.getBatchTime());
		b.setRecordCount(p.getRecordCount());
		b.setPaymentProd(p.getPaymentProduct());
		b.setAuthmod(p.getAuthmod());
		b.setRequestMessageId(p.getRequestMessageId());
		b.setResponseMessageId(p.getResponseMessageId());
		b.setCreatedDate(new Date());
		b.setModifiedDate(new Date());
		b.setMakerId(p.getMakerId());
		b.setCheckerId(p.getCheckerId());
		b.setRemarks(p.getRemarks());
		b.setDebitDate(p.getDebitDate());
		b.setAuthMaker(p.getAuthMaker());
		b.setAuthChecker(p.getAuthChecker());
		return b;
	}

	private PaymentDebitMaster mapDebit(ParsedDebitDTO d, ParsedBatchDTO root) {
		PaymentDebitMaster e = new PaymentDebitMaster();
		e.setAgencyIfsc(d.getAgencyIfsc());
		e.setAgencyAccountNumber(d.getAgencyAccountNumber());
		e.setAgencyAccountName(d.getAgencyAccountName());
		e.setDebitAmount(d.getDebitAmount());
		e.setDebitDate(d.getDebitDate());
		e.setCpsmsDebitTranId(clip(d.getCpsmsDebitTranId(), 16));
		e.setDebitStatus(d.getDebitStatus());
		e.setDebitStan(clip(d.getDebitStan(), 15)); // may be null; StanService will fill
		e.setDebitTranId(d.getDebitTranId());
		e.setDebitTranDate(d.getDebitTranDate());
		e.setSyncStatus(syncStatus(root.getDscResult(), root.isFormatValid()));
		e.setRemarks(null);
		return e;
	}

	private PaymentCreditMaster mapCredit(ParsedCreditDTO c, ParsedBatchDTO root) {
		PaymentCreditMaster e = new PaymentCreditMaster();
		e.setCreditAmount(c.getCreditAmount());
		e.setCreditIfsc(c.getCreditIfsc());
		e.setCreditAccountNumber(c.getCreditAccountNumber());
		e.setCreditAccountName(c.getCreditAccountName());
		e.setCreditAccountAddress(c.getCreditAccountAddress());
		e.setCreditUid(c.getCreditUid());
		e.setCreditBankIin(c.getCreditBankIin());
		e.setPmtMtd(c.getPmtMtd());
		e.setRmtInf(c.getRmtInf());
		e.setCpsmsCreditTran(clip(c.getCpsmsCreditTran(), 16));
		e.setInitiatingUtr(c.getInitiatingUtr());
		e.setNewInitiatingUtr(c.getNewInitiatingUtr());
		e.setReturnedUtr(c.getReturnedUtr());
		e.setTranIdFt(c.getTranIdFt());
		e.setTranDateFt(c.getTranDateFt());
		e.setCreditStatus(c.getCreditStatus());
		e.setCreditStan(clip(c.getCreditStan(), 15)); // may be null; StanService will fill
		e.setSyncStatus(syncStatus(root.getDscResult(), root.isFormatValid()));
		e.setPayRejectReason(c.getPayRejectReason());
		e.setCreReturnReason(c.getCreReturnReason());
		e.setPaymentInitiReqId(c.getPaymentInitiReqId());
		e.setPaymentSuccessReqId(c.getPaymentSuccessReqId());
		e.setFailureRequestId(c.getFailureRequestId());
		e.setCreatedDate(new Date());
		e.setModifiedDate(new Date());
		e.setChecksum(c.getChecksum());
		e.setRemarks(c.getRemarks());
		e.setLeiCode(c.getLeiCode());
		e.setC6346Ini(c.getC6346Ini());
		e.setC6366Ini(c.getC6366Ini());
		e.setC6346Succ(c.getC6346Succ());
		e.setC6366Succ(c.getC6366Succ());
		e.setC6346Rej(c.getC6346Rej());
		e.setC6366Rej(c.getC6366Rej());
		e.setMakerLei(c.getMakerLei());
		e.setCheckerLei(c.getCheckerLei());
		return e;
	}

	private String syncStatus(DscResult dsc, boolean formatValid) {
		return (formatValid && (dsc == DscResult.VALID || dsc == DscResult.NOT_APPLICABLE)) ? "S" : "E";
	}

	private static String clip(String s, int max) {
		if (s == null)
			return null;
		s = s.trim();
		return s.length() <= max ? s : s.substring(0, max);
	}

	private static String trimOrNull(String s) {
		return (s == null) ? null : s.trim();
	}

	private static boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}
}
