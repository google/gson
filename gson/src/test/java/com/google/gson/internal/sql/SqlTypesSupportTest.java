package com.google.gson.internal.sql;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class SqlTypesSupportTest {
  @Test
  public void testSupported() {
    assertThat(SqlTypesSupport.SUPPORTS_SQL_TYPES).isTrue();

    assertThat(SqlTypesSupport.DATE_DATE_TYPE).isNotNull();
    assertThat(SqlTypesSupport.TIMESTAMP_DATE_TYPE).isNotNull();

    assertThat(SqlTypesSupport.DATE_FACTORY).isNotNull();
    assertThat(SqlTypesSupport.TIME_FACTORY).isNotNull();
    assertThat(SqlTypesSupport.TIMESTAMP_FACTORY).isNotNull();
  }
}
