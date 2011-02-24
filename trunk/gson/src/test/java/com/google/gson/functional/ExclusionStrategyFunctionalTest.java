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

package com.google.gson.functional;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.Mode;

import junit.framework.TestCase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Performs some functional tests when Gson is instantiated with some common user defined
 * {@link ExclusionStrategy} objects.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class ExclusionStrategyFunctionalTest extends TestCase {
  private SampleObjectForTest src;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    src = new SampleObjectForTest();
  }

  public void testExclusionStrategySerialization() throws Exception {
    Gson gson = createGson(new MyExclusionStrategy(String.class), null);
    String json = gson.toJson(src);
    assertFalse(json.contains("\"stringField\""));
    assertFalse(json.contains("\"annotatedField\""));
    assertTrue(json.contains("\"longField\""));
  }
  
  public void testExclusionStrategyDeserialization() throws Exception {
    Gson gson = createGson(new MyExclusionStrategy(String.class), null);
    JsonObject json = new JsonObject();
    json.add("annotatedField", new JsonPrimitive(src.annotatedField + 5));
    json.add("stringField", new JsonPrimitive(src.stringField + "blah,blah"));
    json.add("longField", new JsonPrimitive(1212311L));

    SampleObjectForTest target = gson.fromJson(json, SampleObjectForTest.class);
    assertEquals(1212311L, target.longField);

    // assert excluded fields are set to the defaults
    assertEquals(src.annotatedField, target.annotatedField);
    assertEquals(src.stringField, target.stringField);
  }
  
  public void testExclusionStrategyWithMode() throws Exception {
    SampleObjectForTest testObj = new SampleObjectForTest(
        src.annotatedField + 5, src.stringField + "blah,blah",
        src.longField + 655L);

    Gson gson = createGson(new MyExclusionStrategy(String.class), Mode.DESERIALIZE);    
    JsonObject json = gson.toJsonTree(testObj).getAsJsonObject();
    assertEquals(testObj.annotatedField, json.get("annotatedField").getAsInt());
    assertEquals(testObj.stringField, json.get("stringField").getAsString());
    assertEquals(testObj.longField, json.get("longField").getAsLong());

    SampleObjectForTest target = gson.fromJson(json, SampleObjectForTest.class);
    assertEquals(testObj.longField, target.longField);

    // assert excluded fields are set to the defaults
    assertEquals(src.annotatedField, target.annotatedField);
    assertEquals(src.stringField, target.stringField);
  }

  private static Gson createGson(ExclusionStrategy exclusionStrategy, Mode mode) {
    GsonBuilder gsonBuilder = new GsonBuilder();
    if (mode == null) {
      gsonBuilder.setExclusionStrategies(exclusionStrategy);
    } else {
      gsonBuilder.setExclusionStrategies(mode, exclusionStrategy);
    }
    return gsonBuilder
        .serializeNulls()
        .create();
  }
  
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD})
  private static @interface Foo {
    // Field tag only annotation
  }

  private static class SampleObjectForTest {
    @Foo
    private final int annotatedField;
    private final String stringField;
    private final long longField;

    public SampleObjectForTest() {
      this(5, "someDefaultValue", 12345L);
    }
    
    public SampleObjectForTest(int annotatedField, String stringField, long longField) {
      this.annotatedField = annotatedField;
      this.stringField = stringField;
      this.longField = longField;
    }
  }

  private static class MyExclusionStrategy implements ExclusionStrategy {
    private final Class<?> typeToSkip;

    private MyExclusionStrategy(Class<?> typeToSkip) {
      this.typeToSkip = typeToSkip;
    }

    public boolean shouldSkipClass(Class<?> clazz) {
      return (clazz == typeToSkip);
    }

    public boolean shouldSkipField(FieldAttributes f) {
      return f.getAnnotation(Foo.class) != null;
    }
  }
}
