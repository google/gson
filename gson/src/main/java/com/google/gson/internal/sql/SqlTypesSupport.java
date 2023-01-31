/*
 * Copyright (C) 2020 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.internal.sql;

import java.sql.Timestamp;
import java.util.Date;

import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.bind.DefaultDateTypeAdapter.DateType;

/**
 * Encapsulates access to {@code java.sql} types, to allow Gson to
 * work without the {@code java.sql} module being present.
 * No {@link ClassNotFoundException}s will be thrown in case
 * the {@code java.sql} module is not present.
 *
 * <p>If {@link #SUPPORTS_SQL_TYPES} is {@code true}, all other
 * constants of this class will be non-{@code null}. However, if
 * it is {@code false} all other constants will be {@code null} and
 * there will be no support for {@code java.sql} types.
 */
public final class SqlTypesSupport {
  /**
   * {@code true} if {@code java.sql} types are supported,
   * {@code false} otherwise
   */
  public static final boolean SUPPORTS_SQL_TYPES;

  public static final DateType<? extends Date> DATE_DATE_TYPE;
  public static final DateType<? extends Date> TIMESTAMP_DATE_TYPE;

  public static final TypeAdapterFactory DATE_FACTORY;
  public static final TypeAdapterFactory TIME_FACTORY;
  public static final TypeAdapterFactory TIMESTAMP_FACTORY;

  static {
    boolean sqlTypesSupport;
    try {
      Class.forName("java.sql.Date");
      sqlTypesSupport = true;
    } catch (ClassNotFoundException classNotFoundException) {
      sqlTypesSupport = false;
    }
    SUPPORTS_SQL_TYPES = sqlTypesSupport;

    if (SUPPORTS_SQL_TYPES) {
      DATE_DATE_TYPE = new DateType<java.sql.Date>(java.sql.Date.class) {
        @Override protected java.sql.Date deserialize(Date date) {
          return new java.sql.Date(date.getTime());
        }
      };
      TIMESTAMP_DATE_TYPE = new DateType<Timestamp>(Timestamp.class) {
        @Override protected Timestamp deserialize(Date date) {
          return new Timestamp(date.getTime());
        }
      };

      DATE_FACTORY = SqlDateTypeAdapter.FACTORY;
      TIME_FACTORY = SqlTimeTypeAdapter.FACTORY;
      TIMESTAMP_FACTORY = SqlTimestampTypeAdapter.FACTORY;
    } else {
      DATE_DATE_TYPE = null;
      TIMESTAMP_DATE_TYPE = null;

      DATE_FACTORY = null;
      TIME_FACTORY = null;
      TIMESTAMP_FACTORY = null;
    }
  }

  private SqlTypesSupport() {
  }
}
