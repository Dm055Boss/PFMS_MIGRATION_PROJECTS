// src/main/java/com/intech/EpayIniFileGenerator/support/PropsSqlStore.java
package com.intech.support;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class PropsSqlStore implements SqlStore {
  private final Environment env;
  public PropsSqlStore(Environment env) { this.env = env; }

  @Override public String get(String key) {
    String k = "sql." + key;
    String v = env.getProperty(k);
    if (v == null || v.isBlank()) {
      throw new IllegalStateException("Missing SQL property: " + k);
    }
    return v;
  }
}
