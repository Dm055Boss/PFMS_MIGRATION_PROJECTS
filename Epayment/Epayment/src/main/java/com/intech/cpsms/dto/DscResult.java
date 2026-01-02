// src/main/java/com/intech/cpsms/dto/DscResult.java
package com.intech.cpsms.dto;

public enum DscResult {
	VALID, // signature verified
	INVALID, // signature failed
	DSCTAGNOTFOUND, // <Signature> missing but product requires DSC
	NOT_APPLICABLE // product doesn’t require DSC
}
