package com.intech.EpayIniFileGenerator.support;

//src/main/java/com/intech/EpayIniFileGenerator/support/RequestIdFilter.java

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestIdFilter implements Filter {
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		try {
			String rid = UUID.randomUUID().toString().substring(0, 8);
			ThreadContext.put("requestId", rid);
			chain.doFilter(req, res);
		} finally {
			ThreadContext.clearAll();
		}
	}
}
