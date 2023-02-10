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

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests to measure performance for Gson. All tests in this file will be disabled in code. To run
 * them remove disabled_ prefix from the tests and run them.
 * 
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class PerformanceTest {
  private static final int COLLECTION_SIZE = 5000;

  private static final int NUM_ITERATIONS = 100;

  private Gson gson;

  @Before
  public void setUp() throws Exception {
    gson = new Gson();
  }
  
  @Test
  public void testDummy() {
    // This is here to prevent Junit for complaining when we disable all tests.
  }

  @Test
  @Ignore
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
    assertThat(target.message).contains("Error");
    assertThat(target.stackTrace).contains("Yippie");
  }

  private static class ExceptionHolder {
    public final String message;
    public final String stackTrace;
    
    // For use by Gson
    @SuppressWarnings("unused")
    private ExceptionHolder() {
      this("", "");
    }
    public ExceptionHolder(String message, String stackTrace) {
      this.message = message;
      this.stackTrace = stackTrace;
    }
  }

  @SuppressWarnings("unused")
  private static class CollectionEntry {
    final String name;
    final String value;

    // For use by Gson
    private CollectionEntry() {
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
  @Test
  @Ignore
  public void disabled_testLargeCollectionSerialization() {
    int count = 1400000;
    List<CollectionEntry> list = new ArrayList<>(count);
    for (int i = 0; i < count; ++i) {
      list.add(new CollectionEntry("name"+i,"value"+i));
    }    
    gson.toJson(list);
  }
  
  /**
   * Created in response to http://code.google.com/p/google-gson/issues/detail?id=96
   */
  @Test
  @Ignore
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
    assertThat(list).hasSize(count);
  }

  /**
   * Created in response to http://code.google.com/p/google-gson/issues/detail?id=96
   */
  // Last I tested, Gson was able to serialize upto 14MB byte array
  @Test
  @Ignore
  public void disabled_testByteArraySerialization() {
    for (int size = 4145152; true; size += 1036288) {
      byte[] ba = new byte[size];
      for (int i = 0; i < size; ++i) {
        ba[i] = 0x05;
      }
      gson.toJson(ba);
      System.out.printf("Gson could serialize a byte array of size: %d\n", size);
    }
  }
  
  /**
   * Created in response to http://code.google.com/p/google-gson/issues/detail?id=96
   */
  // Last I tested, Gson was able to deserialize a byte array of 11MB
  @Test
  @Ignore
  public void disabled_testByteArrayDeserialization() {
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

// The tests to measure serialization and deserialization performance of Gson
// Based on the discussion at
// http://groups.google.com/group/google-gson/browse_thread/thread/7a50b17a390dfaeb
// Test results: 10/19/2009 
// Serialize classes avg time: 60 ms
// Deserialized classes avg time: 70 ms
// Serialize exposed classes avg time: 159 ms
// Deserialized exposed classes avg time: 173 ms

  @Test
  @Ignore
  public void disabled_testSerializeClasses() {
    ClassWithList c = new ClassWithList("str"); 
    for (int i = 0; i < COLLECTION_SIZE; ++i) { 
      c.list.add(new ClassWithField("element-" + i)); 
    }
    StringWriter w = new StringWriter(); 
    long t1 = System.currentTimeMillis(); 
    for (int i = 0; i < NUM_ITERATIONS; ++i) { 
      gson.toJson(c, w); 
    } 
    long t2 = System.currentTimeMillis(); 
    long avg = (t2 - t1) / NUM_ITERATIONS;
    System.out.printf("Serialize classes avg time: %d ms\n", avg);
  }

  @Test
  @Ignore
  public void disabled_testDeserializeClasses() {
    String json = buildJsonForClassWithList();
    ClassWithList[] target = new ClassWithList[NUM_ITERATIONS];
    long t1 = System.currentTimeMillis(); 
    for (int i = 0; i < NUM_ITERATIONS; ++i) {
      target[i] = gson.fromJson(json, ClassWithList.class);
    }
    long t2 = System.currentTimeMillis(); 
    long avg = (t2 - t1) / NUM_ITERATIONS;
    System.out.printf("Deserialize classes avg time: %d ms\n", avg);
  }

  @Test
  @Ignore
  public void disabled_testLargeObjectSerializationAndDeserialization() {
    Map<String, Long> largeObject = new HashMap<>();
    for (long l = 0; l < 100000; l++) {
      largeObject.put("field" + l, l);
    }
    
    long t1 = System.currentTimeMillis(); 
    String json = gson.toJson(largeObject);
    long t2 = System.currentTimeMillis();
    System.out.printf("Large object serialized in: %d ms\n", (t2 - t1));

    t1 = System.currentTimeMillis(); 
    gson.fromJson(json, new TypeToken<Map<String, Long>>() {}.getType());
    t2 = System.currentTimeMillis();
    System.out.printf("Large object deserialized in: %d ms\n", (t2 - t1));
    
  }

  @Test
  @Ignore
  public void disabled_testSerializeExposedClasses() {
    ClassWithListOfObjects c1 = new ClassWithListOfObjects("str"); 
    for (int i1 = 0; i1 < COLLECTION_SIZE; ++i1) { 
      c1.list.add(new ClassWithExposedField("element-" + i1)); 
    }
    ClassWithListOfObjects c = c1; 
    StringWriter w = new StringWriter(); 
    long t1 = System.currentTimeMillis(); 
    for (int i = 0; i < NUM_ITERATIONS; ++i) { 
      gson.toJson(c, w); 
    } 
    long t2 = System.currentTimeMillis(); 
    long avg = (t2 - t1) / NUM_ITERATIONS;
    System.out.printf("Serialize exposed classes avg time: %d ms\n", avg);
  }

  @Test
  @Ignore
  public void disabled_testDeserializeExposedClasses() {
    String json = buildJsonForClassWithList();
    ClassWithListOfObjects[] target = new ClassWithListOfObjects[NUM_ITERATIONS];
    long t1 = System.currentTimeMillis(); 
    for (int i = 0; i < NUM_ITERATIONS; ++i) {
      target[i] = gson.fromJson(json, ClassWithListOfObjects.class);
    }
    long t2 = System.currentTimeMillis(); 
    long avg = (t2 - t1) / NUM_ITERATIONS;
    System.out.printf("Deserialize exposed classes avg time: %d ms\n", avg);
  }

  @Test
  @Ignore
  public void disabled_testLargeGsonMapRoundTrip() throws Exception {
    Map<Long, Long> original = new HashMap<>();
    for (long i = 0; i < 1000000; i++) {
      original.put(i, i + 1);
    }

    Gson gson = new Gson();
    String json = gson.toJson(original);
    Type longToLong = new TypeToken<Map<Long, Long>>(){}.getType();
    gson.fromJson(json, longToLong);
  }

  private String buildJsonForClassWithList() {
    StringBuilder sb = new StringBuilder("{");
    sb.append("field:").append("'str',");
    sb.append("list:[");
    boolean first = true;
    for (int i = 0; i < COLLECTION_SIZE; ++i) {
      if (first) {
        first = false;
      } else {
        sb.append(",");
      }
      sb.append("{field:'element-" + i + "'}");
    }
    sb.append("]");
    sb.append("}");
    String json = sb.toString();
    return json;
  }

  @SuppressWarnings("unused")
  private static final class ClassWithList { 
    final String field; 
    final List<ClassWithField> list = new ArrayList<>(COLLECTION_SIZE);
    ClassWithList() {
      this(null);
    }
    ClassWithList(String field) {
      this.field = field;
    }
  } 

  @SuppressWarnings("unused")
  private static final class ClassWithField { 
    final String field;
    ClassWithField() {
      this("");
    }
    public ClassWithField(String field) { 
      this.field = field; 
    } 
  }

  @SuppressWarnings("unused")
  private static final class ClassWithListOfObjects { 
    @Expose 
    final String field; 
    @Expose 
    final List<ClassWithExposedField> list = new ArrayList<>(COLLECTION_SIZE);
    ClassWithListOfObjects() {
      this(null);
    }
    ClassWithListOfObjects(String field) {
      this.field = field;
    }
  } 

  @SuppressWarnings("unused")
  private static final class ClassWithExposedField { 
    @Expose 
    final String field;
    ClassWithExposedField() {
      this("");
    }
    ClassWithExposedField(String field) { 
      this.field = field; 
    } 
  }
}
