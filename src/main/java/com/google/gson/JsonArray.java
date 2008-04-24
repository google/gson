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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A class representing an array type in Json. An array is a list 
 * of {@link JsonElement}s each of which can be of a different type. 
 *  
 * @author Inderjeet Singh
 */
public final class JsonArray extends JsonElement implements Iterable<JsonElement> {
  private final List<JsonElement> elements = new LinkedList<JsonElement>();
  
  public void add(JsonElement element) {
    elements.add(element);
  }
  
  public void addAll(JsonArray array) {
    elements.addAll(array.elements);
  }

  public void reverse() {
    Collections.reverse(elements);
  }
  public int size() {
    return elements.size();
  }

  public Iterator<JsonElement> iterator() {
    return elements.iterator();
  }
  
  public JsonElement get(int i) {
    return elements.get(i);
  }
  
  @Override 
  public Number getAsNumber() {
    if (elements.size() == 1) {
      return elements.get(0).getAsNumber();
    } else {
      throw new IllegalStateException();
    }
  }
  
  @Override 
  public String getAsString() {
    if (elements.size() == 1) {
      return elements.get(0).getAsString();
    } else {
      throw new IllegalStateException();
    }
  }
  
  @Override 
  public double getAsDouble() {
    if (elements.size() == 1) {
      return elements.get(0).getAsDouble();
    } else {
      throw new IllegalStateException();
    }
  }
  
  @Override 
  public float getAsFloat() {
    if (elements.size() == 1) {
      return elements.get(0).getAsFloat();
    } else {
      throw new IllegalStateException();
    }
  }
  
  @Override 
  public long getAsLong() {
    if (elements.size() == 1) {
      return elements.get(0).getAsLong();
    } else {
      throw new IllegalStateException();
    }
  }
  
  @Override 
  public int getAsInt() {
    if (elements.size() == 1) {
      return elements.get(0).getAsInt();
    } else {
      throw new IllegalStateException();
    }
  }
  
  @Override 
  public short getAsShort() {
    if (elements.size() == 1) {
      return elements.get(0).getAsShort();
    } else {
      throw new IllegalStateException();
    }
  }
  
  @Override 
  public char getAsChar() {
    if (elements.size() == 1) {
      return elements.get(0).getAsChar();
    } else {
      throw new IllegalStateException();
    }
  }
  
  @Override 
  public boolean getAsBoolean() {
    if (elements.size() == 1) {
      return elements.get(0).getAsBoolean();
    } else {
      throw new IllegalStateException();
    }
  }
  
  @Override 
  public Object getAsObject() {
    if (elements.size() == 1) {
      return elements.get(0).getAsObject();
    } else {
      throw new IllegalStateException();
    }
  }
}
