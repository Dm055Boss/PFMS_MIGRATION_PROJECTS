package com.intech.EpayIniFileGenerator.model;


import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateResult {
	private int generated;
	private String lastMessageId;
	private List<String> errors; // batch-wise error notes
	public int getGenerated() {
		return generated;
	}
	public void setGenerated(int generated) {
		this.generated = generated;
	}
	public String getLastMessageId() {
		return lastMessageId;
	}
	public void setLastMessageId(String lastMessageId) {
		this.lastMessageId = lastMessageId;
	}
	public List<String> getErrors() {
		return errors;
	}
	public void setErrors(List<String> errors) {
		this.errors = errors;
	}
	public GenerateResult(int generated, String lastMessageId, List<String> errors) {
		super();
		this.generated = generated;
		this.lastMessageId = lastMessageId;
		this.errors = errors;
	}
	
	
}
