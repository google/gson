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

import com.google.gson.annotations.Expose;

import junit.framework.TestCase;

/**
 * Unit tests for the regarding functional "@Expose" type tests.
 *
 * @author Joel Leitch
 */
public class ExposeFieldsTest extends TestCase {

  private Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
  }

  public void testNullExposeFieldSerialization() throws Exception {
    ClassWithExposedFields object = new ClassWithExposedFields(null, 1);
    String json = gson.toJson(object);

    assertEquals(object.getExpectedJson(), json);
  }

  public void testArrayWithOneNullExposeFieldObjectSerialization() throws Exception {
    ClassWithExposedFields object1 = new ClassWithExposedFields(1, 1);
    ClassWithExposedFields object2 = new ClassWithExposedFields(null, 1);
    ClassWithExposedFields object3 = new ClassWithExposedFields(2, 2);
    ClassWithExposedFields[] objects = { object1, object2, object3 };

    String json = gson.toJson(objects);
    String expected = new StringBuilder()
        .append('[').append(object1.getExpectedJson()).append(',')
        .append(object2.getExpectedJson()).append(',')
        .append(object3.getExpectedJson()).append(']')
        .toString();

    assertEquals(expected, json);
  }

  public void testExposeAnnotationSerialization() throws Exception {
    ClassWithExposedFields target = new ClassWithExposedFields(1, 2);
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testExposeAnnotationDeserialization() throws Exception {
    String json = '{' + "\"a\":" + 3 + ",\"b\":" + 4 + '}';
    ClassWithExposedFields target = gson.fromJson(json, ClassWithExposedFields.class);

    assertEquals(3, (int) target.a);
    assertNull(target.b);
  }

  public void testNoExposedFieldSerialization() throws Exception {
    ClassWithNoExposedFields obj = new ClassWithNoExposedFields();
    String json = gson.toJson(obj);

    assertEquals("{}", json);
  }

  public void testNoExposedFieldDeserialization() throws Exception {
    String json = '{' + "\"a\":" + 4 + ",\"b\":" + 5 + '}';
    ClassWithNoExposedFields obj = gson.fromJson(json, ClassWithNoExposedFields.class);

    assertEquals(0, obj.a);
    assertEquals(1, obj.b);
  }

  private static class ClassWithExposedFields {
    @Expose private final Integer a;
    private final Integer b;

    ClassWithExposedFields() {
      this(null, null);
    }

    public ClassWithExposedFields(Integer a, Integer b) {
      this.a = a;
      this.b = b;
    }

    public String getExpectedJson() {
      if (a == null) {
        return "{}";
      }
      return '{' + "\"a\":" + a + '}';
    }

    public String getExpectedJsonWithoutAnnotations() {
      StringBuilder stringBuilder = new StringBuilder();
      boolean requiresComma = false;
      stringBuilder.append('{');
      if (a != null) {
        stringBuilder.append("\"a\":").append(a);
        requiresComma = true;
      }
      if (b != null) {
        if (requiresComma) {
          stringBuilder.append(',');
        }
        stringBuilder.append("\"b\":").append(b);
      }
      stringBuilder.append('}');
      return stringBuilder.toString();
    }
  }

  private static class  ClassWithNoExposedFields {
    private final int a = 0;
    private final int b = 1;
  }
}
