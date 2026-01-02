// service/DscValidatorAdapter.java
package com.intech.cpsms.service;

import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class DscValidatorAdapter {

	public String validate(Path xmlPath) throws Exception {
		// Call your existing class directly (no behavior change)
		// return CpsmsDscVerification.valdiatexml(xmlPath.toString());
		// For now, we keep a placeholder to compile; wire your class here:
		return com.intech.cpsms.legacy.CpsmsDscVerification.valdiatexml(xmlPath.toString());
	}
}
