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

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.Before;
import org.junit.Test;

/**
 * Performs some functional tests when Gson is instantiated with some common user defined
 * {@link ExclusionStrategy} objects.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class ExclusionStrategyFunctionalTest {
  private static final ExclusionStrategy EXCLUDE_SAMPLE_OBJECT_FOR_TEST = new ExclusionStrategy() {
    @Override public boolean shouldSkipField(FieldAttributes f) {
      return false;
    }
    @Override public boolean shouldSkipClass(Class<?> clazz) {
      return clazz == SampleObjectForTest.class;
    }
  };

  private SampleObjectForTest src;

  @Before
  public void setUp() throws Exception {
    src = new SampleObjectForTest();
  }

  @Test
  public void testExclusionStrategySerialization() {
    Gson gson = createGson(new MyExclusionStrategy(String.class), true);
    String json = gson.toJson(src);
    assertThat(json).doesNotContain("\"stringField\"");
    assertThat(json).doesNotContain("\"annotatedField\"");
    assertThat(json).contains("\"longField\"");
  }

  @Test
  public void testExclusionStrategySerializationDoesNotImpactDeserialization() {
    String json = "{\"annotatedField\":1,\"stringField\":\"x\",\"longField\":2}";
    Gson gson = createGson(new MyExclusionStrategy(String.class), true);
    SampleObjectForTest value = gson.fromJson(json, SampleObjectForTest.class);
    assertThat(value.annotatedField).isEqualTo(1);
    assertThat(value.stringField).isEqualTo("x");
    assertThat(value.longField).isEqualTo(2);
  }

  @Test
  public void testExclusionStrategyDeserialization() {
    Gson gson = createGson(new MyExclusionStrategy(String.class), false);
    JsonObject json = new JsonObject();
    json.add("annotatedField", new JsonPrimitive(src.annotatedField + 5));
    json.add("stringField", new JsonPrimitive(src.stringField + "blah,blah"));
    json.add("longField", new JsonPrimitive(1212311L));

    SampleObjectForTest target = gson.fromJson(json, SampleObjectForTest.class);
    assertThat(target.longField).isEqualTo(1212311L);

    // assert excluded fields are set to the defaults
    assertThat(target.annotatedField).isEqualTo(src.annotatedField);
    assertThat(target.stringField).isEqualTo(src.stringField);
  }

  @Test
  public void testExclusionStrategySerializationDoesNotImpactSerialization() {
    Gson gson = createGson(new MyExclusionStrategy(String.class), false);
    String json = gson.toJson(src);
    assertThat(json).contains("\"stringField\"");
    assertThat(json).contains("\"annotatedField\"");
    assertThat(json).contains("\"longField\"");
  }

  @Test
  public void testExclusionStrategyWithMode() {
    SampleObjectForTest testObj = new SampleObjectForTest(
        src.annotatedField + 5, src.stringField + "blah,blah",
        src.longField + 655L);

    Gson gson = createGson(new MyExclusionStrategy(String.class), false);
    JsonObject json = gson.toJsonTree(testObj).getAsJsonObject();
    assertThat(json.get("annotatedField").getAsInt()).isEqualTo(testObj.annotatedField);
    assertThat(json.get("stringField").getAsString()).isEqualTo(testObj.stringField);
    assertThat(json.get("longField").getAsLong()).isEqualTo(testObj.longField);

    SampleObjectForTest target = gson.fromJson(json, SampleObjectForTest.class);
    assertThat(target.longField).isEqualTo(testObj.longField);

    // assert excluded fields are set to the defaults
    assertThat(target.annotatedField).isEqualTo(src.annotatedField);
    assertThat(target.stringField).isEqualTo(src.stringField);
  }

  @Test
  public void testExcludeTopLevelClassSerialization() {
    Gson gson = new GsonBuilder()
        .addSerializationExclusionStrategy(EXCLUDE_SAMPLE_OBJECT_FOR_TEST)
        .create();
    assertThat(gson.toJson(new SampleObjectForTest(), SampleObjectForTest.class)).isEqualTo("null");
  }

  @Test
  public void testExcludeTopLevelClassSerializationDoesNotImpactDeserialization() {
    Gson gson = new GsonBuilder()
        .addSerializationExclusionStrategy(EXCLUDE_SAMPLE_OBJECT_FOR_TEST)
        .create();
    String json = "{\"annotatedField\":1,\"stringField\":\"x\",\"longField\":2}";
    SampleObjectForTest value = gson.fromJson(json, SampleObjectForTest.class);
    assertThat(value.annotatedField).isEqualTo(1);
    assertThat(value.stringField).isEqualTo("x");
    assertThat(value.longField).isEqualTo(2);
  }

  @Test
  public void testExcludeTopLevelClassDeserialization() {
    Gson gson = new GsonBuilder()
        .addDeserializationExclusionStrategy(EXCLUDE_SAMPLE_OBJECT_FOR_TEST)
        .create();
    String json = "{\"annotatedField\":1,\"stringField\":\"x\",\"longField\":2}";
    SampleObjectForTest value = gson.fromJson(json, SampleObjectForTest.class);
    assertThat(value).isNull();
  }

  @Test
  public void testExcludeTopLevelClassDeserializationDoesNotImpactSerialization() {
    Gson gson = new GsonBuilder()
        .addDeserializationExclusionStrategy(EXCLUDE_SAMPLE_OBJECT_FOR_TEST)
        .create();
    String json = gson.toJson(new SampleObjectForTest(), SampleObjectForTest.class);
    assertThat(json).contains("\"stringField\"");
    assertThat(json).contains("\"annotatedField\"");
    assertThat(json).contains("\"longField\"");
  }

  private static Gson createGson(ExclusionStrategy exclusionStrategy, boolean serialization) {
    GsonBuilder gsonBuilder = new GsonBuilder();
    if (serialization) {
      gsonBuilder.addSerializationExclusionStrategy(exclusionStrategy);
    } else {
      gsonBuilder.addDeserializationExclusionStrategy(exclusionStrategy);
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

  private static final class MyExclusionStrategy implements ExclusionStrategy {
    private final Class<?> typeToSkip;

    private MyExclusionStrategy(Class<?> typeToSkip) {
      this.typeToSkip = typeToSkip;
    }

    @Override public boolean shouldSkipClass(Class<?> clazz) {
      return (clazz == typeToSkip);
    }

    @Override public boolean shouldSkipField(FieldAttributes f) {
      return f.getAnnotation(Foo.class) != null;
    }
  }
}
