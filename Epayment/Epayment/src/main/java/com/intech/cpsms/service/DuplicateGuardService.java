// DuplicateGuardService.java
package com.intech.cpsms.service;

import org.springframework.stereotype.Service;

import com.intech.cpsms.domain.repo.PaymentBatchMasterRepo;
import com.intech.cpsms.dto.ParsedBatchDTO;

@Service
public class DuplicateGuardService {

	private final PaymentBatchMasterRepo batchRepo;

	public DuplicateGuardService(PaymentBatchMasterRepo batchRepo) {
		this.batchRepo = batchRepo;
	}

	public boolean markIfDuplicate(ParsedBatchDTO parsed) {
		String req = trim(parsed.getRequestMessageId());
		String bn = trim(parsed.getBatchNumber());

		if (req == null || bn == null) {
			// If either is missing, let normal format validation handle it.
			return false;
		}

		long cnt = batchRepo.countByRequestMessageIdAndBatchNumber(req, bn);
		System.out.println("DUPLICATE CHECK: req=" + req + ", batchNo=" + bn + ", count=" + cnt);

		if (cnt > 0) {
			// Force simple NACK without persisting
			parsed.setFormatValid(false);
			System.out.println("count greter than 0....");
//			parsed.addGlobalError("DUPLICATE", "Request/Batch already processed");
			return true;
		}
		return false;
	}

	private static String trim(String s) {
		return (s == null) ? null : s.trim();
	}
}
