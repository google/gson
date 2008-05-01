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
 *
 * @param <T> type for which the deserializer is being registered. It is
 * possible that a deserializer may be asked to deserialize a specific
 * generic type of the T.
 */
public interface JsonDeserializer<T> {
  
  /**
   * Context for deserialization that is passed to a custom deserializer
   * during invocation of its 
   * {@link JsonDeserializer#fromJson(Type, JsonElement, JsonDeserializer.Context)}
   * method
   */
  public interface Context {
    
    /**
     * Invokes default deserialization on the specified object.
     * @param <T> The type of the deserialized object
     * @param typeOfT type of the expected return value
     * @param json the parse tree
     * @return An object of type typeOfT
     * @throws JsonParseException if the parse tree does not contain expected data
     */
    public <T> T deserialize(Type typeOfT, JsonElement json) throws JsonParseException;
  }

  /**
   * @param typeOfT The type of the Object to deserialize to
   * @param json The Json data being deserialized
   * @return a deserialized object of the specified type typeOfT
   *         which is a subclass for {@code T}
   * @throws JsonParseException if json is not in the expected format of
   *         {@code typeofT}
   */
  public T fromJson(Type typeOfT, JsonElement json, Context context) throws JsonParseException;
}
