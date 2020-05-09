package com.google.gson.internal.sql;

import junit.framework.TestCase;

public class SqlTypesSupportTest extends TestCase {
  public void testSupported() {
    assertTrue(SqlTypesSupport.SUPPORTS_SQL_TYPES);

    assertNotNull(SqlTypesSupport.DATE_DATE_TYPE);
    assertNotNull(SqlTypesSupport.TIMESTAMP_DATE_TYPE);

    assertNotNull(SqlTypesSupport.DATE_FACTORY);
    assertNotNull(SqlTypesSupport.TIME_FACTORY);
    assertNotNull(SqlTypesSupport.TIMESTAMP_FACTORY);
  }
}
