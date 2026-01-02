package com.intech.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

//src/main/java/com/intech/EpayIniFileGenerator/config/IniProperties.java

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "ini")
public class IniProperties {
//	private String eatPrefix = "691EAT%";
	private boolean updateDbColumn = false;
	private String bankName = "IDBI BANK";
	private String excludeProduct = "EAT";

	public String getExcludeProduct() {
		return excludeProduct;
	}
	public void setExcludeProduct(String excludeProduct) {
		this.excludeProduct = excludeProduct;
	}
	
	public boolean isUpdateDbColumn() {
		return updateDbColumn;
	}
	public void setUpdateDbColumn(boolean updateDbColumn) {
		this.updateDbColumn = updateDbColumn;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	
	
	
	
}
