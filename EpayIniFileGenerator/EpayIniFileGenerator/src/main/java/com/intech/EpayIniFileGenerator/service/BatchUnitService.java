package com.intech.EpayIniFileGenerator.service;

//src/main/java/com/intech/EpayIniFileGenerator/service/BatchUnitService.java

import com.intech.EpayIniFileGenerator.config.IniProperties;
import com.intech.EpayIniFileGenerator.model.BatchPick;
import com.intech.EpayIniFileGenerator.model.CreditRow;
import com.intech.EpayIniFileGenerator.model.DebitRow;
import com.intech.EpayIniFileGenerator.repo.CpsmsQueryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchUnitService {
	private static final Logger log = LoggerFactory.getLogger(BatchUnitService.class);


	private final CpsmsQueryRepository repo;
	private final CounterService counter;
	private final IniXmlWriter xmlWriter;
	private final IniProperties ini;

	public BatchUnitService(CpsmsQueryRepository repo, CounterService counter, IniXmlWriter xmlWriter,
			IniProperties ini) {
		super();
		this.repo = repo;
		this.counter = counter;
		this.xmlWriter = xmlWriter;
		this.ini = ini;
	}

	@Transactional(rollbackOn = Exception.class)
	public String runOne(BatchPick b, String outDir, String counterPath) throws Exception {
		DebitRow d = repo.findDebit(b.getBatchId());
	      log.info("Batch start: id={} bank={} product={}", b.getBatchId(), b.getBankCode(), b.getProduct());

		if (d == null) {
			throw new IllegalStateException("No eligible debit");
		}
		log.debug("Debit: ifsc={} acc={} amt={} c6346={} auth={} ts={}",
		          d.getIfsc(), d.getAccNo(), d.getDebitAmt(), d.getC6346(), d.getAuthMode(), d.getTs());


		boolean dbfl = !(equalsAny(d.getC6346(), "SUCC", "HOLD"));
		List<CreditRow> credits = repo.findCredits(b.getBatchId(), dbfl);
		if (credits.isEmpty())
			throw new IllegalStateException("No eligible credits");

		String msgId = counter.nextMessageId(counterPath, b.getBankCode(), b.getProduct());
	      log.info("MessageId allocated: {}", msgId);

		xmlWriter.write(outDir, msgId, b, d, credits);
	      log.info("XML written: path={}/{}.xml", outDir, msgId);

//	      if (ini.isUpdateDbColumn()) {
	      int updated=repo.markInitiReqForBatch(b.getBatchId(), msgId);
//			repo.flush(); // surface DB errors now
	      log.info("PAYMENT_INITI_REQ_ID updated rows={}, batchId={}, msgId={}", updated, b.getBatchId(), msgId);

//		}
	      log.info("Batch success");

		return msgId;
	}

	private static boolean equalsAny(String v, String... opts) {
		if (v == null)
			return false;
		for (String o : opts)
			if (v.equalsIgnoreCase(o))
				return true;
		return false;
	}
}
