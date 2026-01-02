package com.intech.regenSuc.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Uses existing PAYINICNTR.properties (or similar) and ONLY updates DATE and
 * PAYSUC. Other keys (PAYINI, PAYREJ, ACK...) are preserved.
 *
 * Example file: PAYREJ=1 PAYINI=1 ACKPENB2C=1 ACKCREPENB2C=1 PAYSUC=-4
 * ACKDEBPENB2C=1 DATE=19072023
 */
@Service
public class MessageIdCounterService {

	private static final Logger log = LogManager.getLogger(MessageIdCounterService.class);

	private final Path counterFile;
	private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("ddMMyyyy");

	public MessageIdCounterService(@Qualifier("SuccRegenProps") Properties successAckProperties) {
		String path = successAckProperties.getProperty("success-ack.counter-file");
		this.counterFile = Paths.get(path);
	}

	/**
	 * Returns next serial as String and updates DATE + PAYSUC in the file. If DATE
	 * is different from today, resets PAYSUC to 1.
	 */
	public synchronized String nextSerialForToday() {
		Properties p = new Properties();
		ensureFileExistsIfMissing();

		try (InputStream in = Files.newInputStream(counterFile)) {
			p.load(in);
		} catch (IOException e) {
			log.error("Failed to load counter file {}. Using defaults.", counterFile, e);
		}

		String today = LocalDate.now().format(dateFmt);
		String propDate = p.getProperty("DATE", today);

		int serial;
		if (today.equals(propDate)) {
			// same day → increment PAYSUC
			serial = parseIntSafe(p.getProperty("PAYSUC"), 0) + 1;
		} else {
			// new day → reset
			serial = 1;
			p.setProperty("DATE", today);
		}

		p.setProperty("PAYSUC", String.valueOf(serial));

		try (OutputStream out = Files.newOutputStream(counterFile)) {
			p.store(out, "Updated by Success Ack generator");
		} catch (IOException e) {
			log.error("Failed to store counter file {}", counterFile, e);
		}

		return String.valueOf(serial);
	}

	private int parseIntSafe(String s, int def) {
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
			return def;
		}
	}

	private void ensureFileExistsIfMissing() {
		try {
			if (!Files.exists(counterFile)) {
				// Usually won't run in your environment (file already exists),
				// but keeps service robust.
				Files.createDirectories(counterFile.getParent());
				Properties p = new Properties();
				p.setProperty("DATE", LocalDate.now().format(dateFmt));
				p.setProperty("PAYSUC", "0");
				try (OutputStream out = Files.newOutputStream(counterFile)) {
					p.store(out, "Init Success Ack counter");
				}
			}
		} catch (IOException e) {
			log.error("Failed to initialize counter file {}", counterFile, e);
		}
	}
}
