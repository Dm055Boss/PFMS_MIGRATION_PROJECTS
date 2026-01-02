// service/AuditService.java
package com.intech.cpsms.service;

import com.intech.cpsms.domain.entity.CpsmsAudit;
import com.intech.cpsms.domain.repo.CpsmsAuditRepo;
import com.intech.cpsms.dto.AckVariant;
import com.intech.cpsms.dto.ParsedBatchDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class AuditService {

  private final CpsmsAuditRepo repo;

  public AuditService(CpsmsAuditRepo repo) {
    this.repo = repo;
  }

  @Transactional
  public void writeHeaderAudit(ParsedBatchDTO parsed, String responseMessageId, AckVariant variant) {
    CpsmsAudit a = new CpsmsAudit();
    a.setRequestMessageId(parsed.getRequestMessageId());
    a.setResponseMessageId(responseMessageId);
    a.setBatchNumber(parsed.getBatchNumber());

    // You can decide your own codes; here: 'A' for ACK, 'N' for NACK
    String ackCode = (variant == AckVariant.SUCCESS) ? "A" : "N";
    a.setAckCode(ackCode);

    a.setErrorCode(parsed.getTopErrorCode()); // null for success
    a.setBatchStatus(null);                   // set if you have a status code
    Date now = new Date();
    a.setDateTime(now);
    a.setResponseMessageTime(now);
    a.setAckReceivedDate(now);

    a.setMakerId(parsed.getMakerId());
    a.setCheckerId(parsed.getCheckerId());
    a.setRemarks1(parsed.getTopErrorRemarks()); // short message if any
    a.setRemarks2(parsed.getRemarks());         // whatever you want to carry over

    a.setBankId("01");

    // Keep tran-ids empty at header level (or pick first ones if you prefer)
    a.setCpsmsDebitTranId(null);
    a.setCpsmsCreditTranId(null);

    repo.save(a);
  }
}
