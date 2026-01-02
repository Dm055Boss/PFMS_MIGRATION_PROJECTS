// src/main/java/com/intech/EpayIniFileGenerator/support/SqlStore.java
package com.intech.EpayIniFileGenerator.support;

public interface SqlStore {
  String get(String key); // e.g., "batch.distinct"
}
