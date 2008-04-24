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

import com.google.common.base.Preconditions;

/**
 * A class representing a Json primitive value. A primitive value 
 * is either a String, a Java primitive, or a Java primitive 
 * wrapper type.
 *  
 * @author Inderjeet Singh
 */
public final class JsonPrimitive extends JsonElement {
  
  private Object value;
  
  @SuppressWarnings("unchecked")
  private static final Class[] PRIMITIVE_TYPES = {int.class, long.class, short.class, float.class, 
    double.class, byte.class, boolean.class, Integer.class, Long.class, Short.class, Float.class, 
    Double.class, Byte.class, Boolean.class}; 

  public JsonPrimitive(Boolean bool) {
    this.value = bool;
  }
  
  public JsonPrimitive(Number number) {
    this.value = number;
  }
  
  public JsonPrimitive(String string) {
    this.value = string;
  }
  
  public JsonPrimitive(Character c) {
    this.value = String.valueOf(c);
  }
  
  public JsonPrimitive(char c) {
    this.value = String.valueOf(c);
  }
  
  public JsonPrimitive(Object primitive) {
    if (primitive instanceof Character) {
      // convert characters to strings since in JSON, characters are represented as a single 
      // chacater string 
      char c = ((Character)primitive).charValue();
      this.value = String.valueOf(c);
    } else {
      Preconditions.checkArgument(primitive instanceof Number || primitive instanceof String ||
          isPrimitive(primitive));
      this.value = primitive;
    }
  }
  
  @SuppressWarnings("unchecked")
  private boolean isPrimitive(Object primitive) {
    Class<?> classOfPrimitive = primitive.getClass();
    
    for (Class standardPrimitive : PRIMITIVE_TYPES) {
      if (classOfPrimitive.isAssignableFrom(standardPrimitive)) {
        return true;
      }
    }
    return false;
  }

  public boolean isBoolean() {
    return value instanceof Boolean;
  }
  
  @Override
  public Boolean getAsBooleanWrapper() {
    return (Boolean) value;
  }
  
  @Override
  public boolean getAsBoolean() {
    return ((Boolean) value).booleanValue();
  }
  
  public boolean isNumber() {
    return value instanceof Number;
  }
  
  @Override
  public Number getAsNumber() {
    return (Number) value;
  }
  
  public boolean isString() {
    return value instanceof String;
  }
  
  @Override
  public String getAsString() {
    return (String) value;
  }

  @Override
  public double getAsDouble() {
    return ((Number) value).doubleValue();
  }
  
  @Override
  public float getAsFloat() {
    return ((Number) value).floatValue();
  }
  
  @Override
  public long getAsLong() {
    return ((Number) value).longValue();
  }
  
  @Override
  public short getAsShort() {
    return ((Number) value).shortValue();
  }
  
  @Override
  public int getAsInt() {
    return ((Number) value).intValue();
  }
  
  @Override
  public Object getAsObject() {
    return value;
  }

  @Override
  protected void toJson(StringBuilder sb) {
    if (value != null) {
      if (value instanceof String) {
        sb.append('"');
        sb.append(value);
        sb.append('"');
        
      } else {
        sb.append(value);
      }
    }
  }
}
