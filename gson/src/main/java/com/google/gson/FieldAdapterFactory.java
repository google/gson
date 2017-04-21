/*
 * Copyright (C) 2017 Gson Authors
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import com.google.gson.annotations.JsonAdapter;

/**
 * Factory for producing {@link TypeAdapter} that only read / write specific fields
 * with some given annotation.
 * <p>
 * Configured through {@link GsonBuilder#registerFieldAdapterFactory}.
 * <p>
 * Normally a field needs only one adapter, so it doesn't make sense if both this
 * and {@link JsonAdapter} are applied with a single field. In that case, this will
 * be ignored.
 *
 * @author Floyd Wan
 * @since 2.9.0
 */
public interface FieldAdapterFactory {

  /**
   * Create a {@link TypeAdapter} for a field with a special annotation.
   *
   * @param context the Gson instance as the context
   * @param annotation the annotation instance on the given {@code field}
   * @param field the field needs to be investigated
   * @param <T> type of this field
   * @return a {@link TypeAdapter} to read / write this very field.
   */
  <T> TypeAdapter<T> create(Gson context, Annotation annotation, Field field);
}