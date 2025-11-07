/*
 * Copyright (C) 2025 Google Inc.
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

package com.google.gson;

import com.google.gson.internal.bind.DefaultDateTypeAdapter;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.internal.sql.SqlTypesSupport;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

final class GsonBuilderHelper {

  private GsonBuilderHelper() {}

  static <E> List<E> newImmutableList(Collection<E> collection) {
    if (collection.isEmpty()) {
      return Collections.emptyList();
    }
    if (collection.size() == 1) {
      return Collections.singletonList(
          collection instanceof List
              ? ((List<E>) collection).get(0)
              : collection.iterator().next());
    }
    @SuppressWarnings("unchecked")
    List<E> list = (List<E>) Collections.unmodifiableList(Arrays.asList(collection.toArray()));
    return list;
  }

  static TypeAdapter<Number> doubleAdapter(boolean serializeSpecialFloatingPointValues) {
    return serializeSpecialFloatingPointValues ? TypeAdapters.DOUBLE : TypeAdapters.DOUBLE_STRICT;
  }

  static TypeAdapter<Number> floatAdapter(boolean serializeSpecialFloatingPointValues) {
    return serializeSpecialFloatingPointValues ? TypeAdapters.FLOAT : TypeAdapters.FLOAT_STRICT;
  }

  static void addDateTypeAdapters(
      String datePattern, int dateStyle, int timeStyle, List<TypeAdapterFactory> factories) {
    TypeAdapterFactory dateAdapterFactory;
    boolean sqlTypesSupported = SqlTypesSupport.SUPPORTS_SQL_TYPES;
    TypeAdapterFactory sqlTimestampAdapterFactory = null;
    TypeAdapterFactory sqlDateAdapterFactory = null;

    if (datePattern != null && !datePattern.trim().isEmpty()) {
      dateAdapterFactory = DefaultDateTypeAdapter.DateType.DATE.createAdapterFactory(datePattern);

      if (sqlTypesSupported) {
        sqlTimestampAdapterFactory =
            SqlTypesSupport.TIMESTAMP_DATE_TYPE.createAdapterFactory(datePattern);
        sqlDateAdapterFactory = SqlTypesSupport.DATE_DATE_TYPE.createAdapterFactory(datePattern);
      }
    } else if (dateStyle != DateFormat.DEFAULT || timeStyle != DateFormat.DEFAULT) {
      dateAdapterFactory =
          DefaultDateTypeAdapter.DateType.DATE.createAdapterFactory(dateStyle, timeStyle);

      if (sqlTypesSupported) {
        sqlTimestampAdapterFactory =
            SqlTypesSupport.TIMESTAMP_DATE_TYPE.createAdapterFactory(dateStyle, timeStyle);
        sqlDateAdapterFactory =
            SqlTypesSupport.DATE_DATE_TYPE.createAdapterFactory(dateStyle, timeStyle);
      }
    } else {
      return;
    }

    factories.add(dateAdapterFactory);
    if (sqlTypesSupported) {
      factories.add(sqlTimestampAdapterFactory);
      factories.add(sqlDateAdapterFactory);
    }
  }
}
