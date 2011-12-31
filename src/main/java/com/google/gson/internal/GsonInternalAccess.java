/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.internal;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

/**
 * Internal-only APIs of Gson available only to other classes in Gson.
 */
public abstract class GsonInternalAccess {
  public static GsonInternalAccess INSTANCE;

  /**
   * Returns a type adapter for {@code} type that isn't {@code skipPast}. This
   * can be used for type adapters to compose other, simpler type adapters.
   *
   * @throws IllegalArgumentException if this GSON cannot serialize and
   *     deserialize {@code type}.
   */
  public abstract <T> TypeAdapter<T> getNextAdapter(
      Gson gson, TypeAdapterFactory skipPast, TypeToken<T> type);
}
