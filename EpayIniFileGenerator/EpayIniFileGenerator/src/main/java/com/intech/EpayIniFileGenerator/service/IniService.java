// src/main/java/com/intech/EpayIniFileGenerator/service/IniService.java
package com.intech.EpayIniFileGenerator.service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.intech.EpayIniFileGenerator.config.IniProperties;
//import com.intech.EpayIniFileGenerator.controller.IniController;
import com.intech.EpayIniFileGenerator.model.BatchPick;
import com.intech.EpayIniFileGenerator.model.GenerateResult;
import com.intech.EpayIniFileGenerator.repo.CpsmsQueryRepository;

@Service
public class IniService {
	private static final Logger log = LoggerFactory.getLogger(IniService.class);

	private final CpsmsQueryRepository repo;
	private final FolderMap folderMap;
	private final BatchUnitService batchUnit;
	private final IniProperties ini;

	public IniService(CpsmsQueryRepository repo, FolderMap folderMap, BatchUnitService batchUnit, IniProperties ini) {
		this.repo = repo;
		this.folderMap = folderMap;
		this.batchUnit = batchUnit;
		this.ini = ini;
	}

	/** Top-level: NO @Transactional */
	public GenerateResult generate() throws Exception {
		var fm = folderMap.resolve();
		log.info("Output dir={} | Counter path={}", fm.outputDir(), fm.counterPath());

		Files.createDirectories(Paths.get(fm.outputDir()));

		List<String> errors = new ArrayList<>();
		int gen = 0;
		String last = null;

		List<BatchPick> batches = repo.findDistinctBatches(ini.getExcludeProduct());
		log.info("Picked {} eligible batches (excludeProduct={})", batches.size(), ini.getExcludeProduct());

		System.out.println("batches  >> " + batches);
		for (BatchPick b : batches) {
			try {
				String msgId = batchUnit.runOne(b, fm.outputDir(), fm.counterPath());
				System.out.println("msgId >> " + msgId);
				gen++;
				last = msgId;
			} catch (Exception ex) {
				errors.add("Batch " + b.getBatchNumber() + " failed: " + rootCause(ex));
			}
		}
		return new GenerateResult(gen, last, errors);
	}

	private static String rootCause(Throwable t) {
		Throwable c = t;
		while (c.getCause() != null)
			c = c.getCause();
		return c.getMessage();
	}
}
