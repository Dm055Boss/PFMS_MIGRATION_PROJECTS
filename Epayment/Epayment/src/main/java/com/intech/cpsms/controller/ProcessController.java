// controller/ProcessController.java
package com.intech.cpsms.controller;

import com.intech.cpsms.service.FileProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ProcessController {

	private final FileProcessingService fileProcessingService;
	

	public ProcessController(FileProcessingService fileProcessingService) {
		super();
		this.fileProcessingService = fileProcessingService;
	}


	@PostMapping("/process")
	public ResponseEntity<String> processAll() {
		int processed = fileProcessingService.processAll();
		return ResponseEntity.ok("Processed files: " + processed);
	}
}
