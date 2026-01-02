//package com.intech.EpayIniFileGenerator.controller;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.intech.EpayIniFileGenerator.model.GenerateResult;
//import com.intech.EpayIniFileGenerator.service.IniService;
//
//import lombok.RequiredArgsConstructor;
//
//@RestController
//@RequiredArgsConstructor
//public class IniController {
//	private static final Logger log = LoggerFactory.getLogger(IniController.class);
//
//	private final IniService service;
//
//	public IniController(IniService service) {
//		super();
//		this.service = service;
//	}
//
//	@GetMapping("/ini/generate")
//	public GenerateResult generate() throws Exception {
//		 long t0 = System.currentTimeMillis();
//		    log.info("INI generate: started");
//		    var result = service.generate();
//		    log.info("INI generate: done, files={}, lastMsg={}", result.getGenerated(), result.getLastMessageId());
//		    log.debug("Errors: {}", result.getErrors());
//		    return result;
//	}
//}
