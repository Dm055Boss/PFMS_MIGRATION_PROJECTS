// src/main/java/com/intech/cpsms/dto/AckVariant.java
package com.intech.cpsms.dto;

/** Which response builder to use. */
public enum AckVariant {
  SUCCESS,          // createAcknowledgeWithSuccess(...)
  NACK_WITH_TX,     // createAcknowledgeWithTransactions(...)
  NACK_SIMPLE       // createAcknowledge(...)
}
