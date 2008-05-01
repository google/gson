/*
 * Copyright (C) 2008 Google Inc.
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


import java.lang.reflect.Type;

/**
 * Interface representing a custom deserializer for Json.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 *
 * @param <T> type for which the deserializer is being registered. It is
 * possible that a deserializer may be asked to deserialize a specific
 * generic type of the T.
 */
public interface JsonDeserializer<T> {
  
  /**
   * @param typeOfT The type of the Object to deserialize to
   * @param json The Json data being deserialized
   * @return a deserialized object of the specified type typeOfT
   *         which is a subclass for {@code T}
   * @throws JsonParseException if json is not in the expected format of
   *         {@code typeofT}
   */
  public T fromJson(Type typeOfT, JsonElement json, JsonDeserializationContext context) 
      throws JsonParseException;
}
