package com.intech.cpsms.service;

import com.intech.cpsms.domain.repo.PaymentCreditMasterRepo;
import com.intech.cpsms.domain.repo.PaymentDebitMasterRepo;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class StanService {

	private final SecureRandom rnd = new SecureRandom();
	private final PaymentDebitMasterRepo debitRepo;
	private final PaymentCreditMasterRepo creditRepo;

	public StanService(PaymentDebitMasterRepo debitRepo, PaymentCreditMasterRepo creditRepo) {
		this.debitRepo = debitRepo;
		this.creditRepo = creditRepo;
	}

	/**
	 * 15-digit debit STAN, collision-checked in DB. Mirrors legacy
	 * getRandomStan-style behavior.
	 */
	public String newDebitStan() {
		return generateUniqueStan(true);
	}

	/**
	 * 15-digit credit STAN, collision-checked in DB. Mirrors legacy
	 * rsCreditStan-style behavior.
	 */
	public String newCreditStan() {
		return generateUniqueStan(false);
	}

	private String generateUniqueStan(boolean forDebit) {
		for (int attempts = 0; attempts < 10; attempts++) {
			long n = Math.abs(rnd.nextLong()) % 1_000_000_000_000_000L; // up to 15 digits
			String stan = String.format("%015d", n);
			boolean exists = forDebit ? debitRepo.existsByDebitStan(stan) : creditRepo.existsByCreditStan(stan);
			if (!exists) {
				return stan;
			}
		}
		// Extremely unlikely fallback: time-based, still 15 digits
		long fb = System.currentTimeMillis() % 1_000_000_000_000_000L;
		return String.format("%015d", fb);
	}
}
