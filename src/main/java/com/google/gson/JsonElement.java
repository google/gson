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

/**
 * A class representing an element of Json. It could either
 * be a {@link JsonObject}, a {@link JsonArray} or a 
 * {@link JsonPrimitive}. 
 *  
 * @author Inderjeet Singh
 */
public abstract class JsonElement {
  
  public boolean isArray() {
    return this instanceof JsonArray;
  }
  
  public boolean isObject() {
    return this instanceof JsonObject;
  }

  public boolean isPrimitive() {
    return this instanceof JsonPrimitive;
  }
  
  /**
   * @return convenience method to get this element as a {@link JsonObject}
   */
  public JsonObject getAsJsonObject() {
    return (JsonObject) this;
  }
  
  public JsonArray getAsJsonArray() {
    return (JsonArray) this;
  }
  
  public JsonPrimitive getAsJsonPrimitive() {
    return (JsonPrimitive) this;
  }

  public boolean getAsBoolean() {
    throw new UnsupportedOperationException();
  }
  
  public Boolean getAsBooleanWrapper() {
    throw new UnsupportedOperationException();
  }
  
  public Number getAsNumber() {
    throw new UnsupportedOperationException();
  }
  
  public String getAsString() {
    throw new UnsupportedOperationException();
  }
  
  public double getAsDouble() {
    throw new UnsupportedOperationException();
  }
  
  public float getAsFloat() {
    throw new UnsupportedOperationException();
  }
  
  public long getAsLong() {
    throw new UnsupportedOperationException();
  }
  
  public int getAsInt() {
    throw new UnsupportedOperationException();
  }
  
  public short getAsShort() {
    throw new UnsupportedOperationException();
  }
  
  public Object getAsObject() {
    throw new UnsupportedOperationException();    
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    toString(sb);
    return sb.toString();
  }
  
  protected abstract void toString(StringBuilder sb);
  
  void accept(JsonElementVisitor visitor) {
    JsonTreeNavigator navigator = new JsonTreeNavigator(visitor);
    navigator.navigate(this);
  }
}
