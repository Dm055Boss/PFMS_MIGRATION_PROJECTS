package com.intech.service;

//src/main/java/com/intech/cpsmsini/service/FolderMap.java

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FolderMap {
	@Value("${legacy.folderMapPath:}")
	private String folderMapPath;
	@Value("${legacy.outputDir:}")
	private String outputDirOverride;
	@Value("${legacy.counterPath:}")
	private String counterOverride;
	private final String outputDir;

	public FolderMap(@Value("${ini.output-dir}") String outputDir) {
		// keep as-is; Windows paths are fine. Ensure no trailing spaces.
		this.outputDir = outputDir == null ? "" : outputDir.trim();
	}

	/** Returns the directory where INI XML should be written. */
	public String outputDir() {
		return outputDir;
	}

	public record Res(String outputDir, String counterPath) {
	}

	public Res resolve() throws IOException {
		if (nb(outputDirOverride) && nb(counterOverride))
			return new Res(outputDirOverride, counterOverride);
		Properties p = new Properties();
		try (InputStream in = Files.newInputStream(Paths.get(folderMapPath))) {
			p.load(in);
		}
		String out = nb(outputDirOverride) ? outputDirOverride
				: req(p.getProperty("PaymentInitiatedData_ToCPSMS"), "PaymentInitiatedData_ToCPSMS");
		String cn = nb(counterOverride) ? counterOverride : req(p.getProperty("counter"), "counter");
		return new Res(out, cn);
	}

	private static boolean nb(String s) {
		return s != null && !s.isBlank();
	}

	private static String req(String v, String k) {
		if (v == null || v.isBlank())
			throw new IllegalStateException(k + " missing");
		return v;
	}

	public String getFolderMapPath() {
		return folderMapPath;
	}

	public void setFolderMapPath(String folderMapPath) {
		this.folderMapPath = folderMapPath;
	}

	public String getOutputDirOverride() {
		return outputDirOverride;
	}

	public void setOutputDirOverride(String outputDirOverride) {
		this.outputDirOverride = outputDirOverride;
	}

	public String getCounterOverride() {
		return counterOverride;
	}

	public void setCounterOverride(String counterOverride) {
		this.counterOverride = counterOverride;
	}

}
