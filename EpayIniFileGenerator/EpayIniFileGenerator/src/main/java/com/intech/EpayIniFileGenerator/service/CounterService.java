package com.intech.EpayIniFileGenerator.service;

//src/main/java/com/intech/cpsmsini/service/CounterService.java

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.springframework.stereotype.Component;

@Component
public class CounterService {
	private static final SimpleDateFormat DMY = new SimpleDateFormat("ddMMyyyy");

	public synchronized String nextMessageId(String propsPath, String bankCode, String product) {
		String today = DMY.format(new Date());
		Properties p = new Properties();
		Path f = Paths.get(propsPath);
		try {
			if (Files.exists(f))
				try (InputStream in = Files.newInputStream(f)) {
					p.load(in);
				}
			String last = p.getProperty("DATE", "");
			int n = Integer.parseInt(p.getProperty("PAYINI", "0"));
			p.setProperty("DATE", today);
			p.setProperty("PAYINI", (!today.equals(last)) ? "1" : String.valueOf(n + 1));
			try (OutputStream out = Files.newOutputStream(f)) {
				p.store(out, null);
			}
			return bankCode + product + "INIPAY" + today + p.getProperty("PAYINI");
		} catch (Exception e) {
			return bankCode + product + "INIPAY" + today + "1";
		}
	}
}
