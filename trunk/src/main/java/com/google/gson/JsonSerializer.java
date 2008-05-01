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
 * Interface representing a custom serializer for Json.   
 * 
 * @author Inderjeet Singh
 *
 * @param <T> type for which the serializer is being registered. It is possible
 * that a serializer may be asked to serialize a specific generic type of the
 * T. 
 */
public interface JsonSerializer<T> {
  
  /**
   * Context for serialization that is passed to a custom 
   * serializer during invocation of its 
   * {@link JsonSerializer#toJson(Object, Type, JsonSerializer.Context)} method
   */
  public interface Context {
    
    /**
     * Invokes default serialization on the specified object. 
     * @param src the object that needs to be serialized
     * @return a tree of {@link JsonElement}s corresponding to the serialized form of {@code src}
     */
    public JsonElement serialize(Object src);
    
    /**
     * Invokes default serialization on the specified object passing the 
     * specific type information. This method should be used if src is 
     * of a generic type
     * @param src the object that needs to be serialized
     * @param typeOfSrc the actual genericized type of src object
     * @return a tree of {@link JsonElement}s corresponding to the serialized form of {@code src}
     */
    public JsonElement serialize(Object src, Type typeOfSrc);
  }
  
  /**
   * @param src the object that needs to be converted to Json
   * @param typeOfSrc the actual type (fully genericized version) of 
   *        the source object
   * @return a JsonElement corresponding to the specified object 
   */
  public JsonElement toJson(T src, Type typeOfSrc, Context context);
}
