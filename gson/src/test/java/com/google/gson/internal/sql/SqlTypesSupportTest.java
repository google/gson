package com.google.gson.internal.sql;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SqlTypesSupportTest {
  @Test
  public void testSupported() {
    assertTrue(SqlTypesSupport.SUPPORTS_SQL_TYPES);

    assertNotNull(SqlTypesSupport.DATE_DATE_TYPE);
    assertNotNull(SqlTypesSupport.TIMESTAMP_DATE_TYPE);

    assertNotNull(SqlTypesSupport.DATE_FACTORY);
    assertNotNull(SqlTypesSupport.TIME_FACTORY);
    assertNotNull(SqlTypesSupport.TIMESTAMP_FACTORY);
  }
}
