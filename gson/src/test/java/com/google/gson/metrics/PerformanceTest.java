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

package com.google.gson.metrics;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import junit.framework.TestCase;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests to measure performance for Gson. All tests in this file will be disabled in code. To run
 * them remove disabled_ prefix from the tests and run them.
 * 
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class PerformanceTest extends TestCase {
  private Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();
  }
  
  public void testDummy() {    
    // This is here to prevent Junit for complaining when we disable all tests.
  }  

  public void disabled_testStringDeserialization() {    
    StringBuilder sb = new StringBuilder(8096);
    sb.append("Error Yippie");

    while (true) {
      try {
        String stackTrace = sb.toString();
        sb.append(stackTrace);
        String json = "{\"message\":\"Error message.\"," + "\"stackTrace\":\"" + stackTrace + "\"}";
        parseLongJson(json);
        System.out.println("Gson could handle a string of size: " + stackTrace.length());
      } catch (JsonParseException expected) {
        break;
      }
    }
  }
  
  private void parseLongJson(String json) throws JsonParseException {
    ExceptionHolder target = gson.fromJson(json, ExceptionHolder.class);
    assertTrue(target.message.contains("Error"));
    assertTrue(target.stackTrace.contains("Yippie"));
  }

  private static class ExceptionHolder {
    public final String message;
    public final String stackTrace;
    public ExceptionHolder() {
      this("", "");
    }
    public ExceptionHolder(String message, String stackTrace) {
      this.message = message;
      this.stackTrace = stackTrace;
    }
  }

  private static class CollectionEntry {
    final String name;
    final String value;
    
    CollectionEntry() {
      this(null, null);
    }
    
    CollectionEntry(String name, String value) {
      this.name = name;
      this.value = value;
    }
  }
  
  /**
   * Created in response to http://code.google.com/p/google-gson/issues/detail?id=96
   */
  public void disabled_testLargeCollectionSerialization() {
    int count = 1400000;
    List<CollectionEntry> list = new ArrayList<CollectionEntry>(count);
    for (int i = 0; i < count; ++i) {
      list.add(new CollectionEntry("name"+i,"value"+i));
    }    
    gson.toJson(list);
  }
  
  /**
   * Created in response to http://code.google.com/p/google-gson/issues/detail?id=96
   */
  public void disabled_testLargeCollectionDeserialization() {
    StringBuilder sb = new StringBuilder();
    int count = 87000;
    boolean first = true;
    sb.append('[');
    for (int i = 0; i < count; ++i) {
      if (first) {
        first = false;
      } else {
        sb.append(',');
      }
      sb.append("{name:'name").append(i).append("',value:'value").append(i).append("'}");
    }    
    sb.append(']');
    String json = sb.toString();
    Type collectionType = new TypeToken<ArrayList<CollectionEntry>>(){}.getType();    
    List<CollectionEntry> list = gson.fromJson(json, collectionType);       
    assertEquals(count, list.size());
  }

  /**
   * Created in response to http://code.google.com/p/google-gson/issues/detail?id=96
   */
  // Last I tested, Gson was able to serialize upto 14MB byte array
  public void disable_testByteArraySerialization() {
    for (int size = 4145152; true; size += 1036288) {
      byte[] ba = new byte[size];
      for (int i = 0; i < size; ++i) {
        ba[i] = 0x05;
      }
      String json = gson.toJson(ba);
      System.out.printf("Gson could serialize a byte array of size: %d\n", size);
    }
  }
  
  /**
   * Created in response to http://code.google.com/p/google-gson/issues/detail?id=96
   */
  // Last I tested, Gson was able to deserialize a byte array of 11MB
  public void disable_testByteArrayDeserialization() {
    for (int numElements = 10639296; true; numElements += 16384) {
      StringBuilder sb = new StringBuilder(numElements*2);
      sb.append("[");
      boolean first = true;
      for (int i = 0; i < numElements; ++i) {
        if (first) {
          first = false;
        } else {
          sb.append(",");
        }
        sb.append("5");
      }
      sb.append("]");
      String json = sb.toString();
      byte[] ba = gson.fromJson(json, byte[].class);
      System.out.printf("Gson could deserialize a byte array of size: %d\n", ba.length);
    }
  }
}
