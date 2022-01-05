package com.google.gson.internal.sql;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SqlTypesSupportTest {
  @Test
  void testSupported() {
    assertTrue(SqlTypesSupport.SUPPORTS_SQL_TYPES);

    assertNotNull(SqlTypesSupport.DATE_DATE_TYPE);
    assertNotNull(SqlTypesSupport.TIMESTAMP_DATE_TYPE);

    assertNotNull(SqlTypesSupport.DATE_FACTORY);
    assertNotNull(SqlTypesSupport.TIME_FACTORY);
    assertNotNull(SqlTypesSupport.TIMESTAMP_FACTORY);
  }
}
