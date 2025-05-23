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
import static org.junit.Assert.assertThrows;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.common.TestTypes.BagOfPrimitives;
import com.google.gson.common.TestTypes.Nested;
import com.google.gson.reflect.TypeToken;
import java.io.EOFException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/**
 * Functional tests for that use JsonParser and related Gson methods
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class JsonParserTest {
  private Gson gson;

  @Before
  public void setUp() throws Exception {
    gson = new Gson();
  }

  @Test
  public void testParseInvalidJson() {
    var e = assertThrows(JsonSyntaxException.class, () -> gson.fromJson("[[]", Object[].class));
    assertThat(e).hasCauseThat().isInstanceOf(EOFException.class);
  }

  @Test
  public void testDeserializingCustomTree() {
    JsonObject obj = new JsonObject();
    obj.addProperty("stringValue", "foo");
    obj.addProperty("intValue", 11);
    BagOfPrimitives target = gson.fromJson(obj, BagOfPrimitives.class);
    assertThat(target.intValue).isEqualTo(11);
    assertThat(target.stringValue).isEqualTo("foo");
  }

  @Test
  public void testBadTypeForDeserializingCustomTree() {
    JsonObject obj = new JsonObject();
    obj.addProperty("stringValue", "foo");
    obj.addProperty("intValue", 11);
    JsonArray array = new JsonArray();
    array.add(obj);
    // BagOfPrimitives should not be an array
    assertThrows(JsonParseException.class, () -> gson.fromJson(array, BagOfPrimitives.class));
  }

  @Test
  public void testBadFieldTypeForCustomDeserializerCustomTree() {
    JsonArray array = new JsonArray();
    array.add(new JsonPrimitive("blah"));
    JsonObject obj = new JsonObject();
    obj.addProperty("stringValue", "foo");
    obj.addProperty("intValue", 11);
    obj.add("longValue", array);

    // `longValue` should not be an array
    assertThrows(JsonParseException.class, () -> gson.fromJson(obj, BagOfPrimitives.class));
  }

  @Test
  public void testBadFieldTypeForDeserializingCustomTree() {
    JsonArray array = new JsonArray();
    array.add(new JsonPrimitive("blah"));
    JsonObject primitive1 = new JsonObject();
    primitive1.addProperty("string", "foo");
    primitive1.addProperty("intValue", 11);

    JsonObject obj = new JsonObject();
    obj.add("primitive1", primitive1);
    obj.add("primitive2", array);

    // Nested has field BagOfPrimitives which is not an array
    assertThrows(JsonParseException.class, () -> gson.fromJson(obj, Nested.class));
  }

  @Test
  public void testChangingCustomTreeAndDeserializing() {
    StringReader json =
        new StringReader("{'stringValue':'no message','intValue':10,'longValue':20}");
    JsonObject obj = (JsonObject) JsonParser.parseReader(json);
    obj.remove("stringValue");
    obj.addProperty("stringValue", "fooBar");
    BagOfPrimitives target = gson.fromJson(obj, BagOfPrimitives.class);
    assertThat(target.intValue).isEqualTo(10);
    assertThat(target.longValue).isEqualTo(20);
    assertThat(target.stringValue).isEqualTo("fooBar");
  }

  @Test
  public void testExtraCommasInArrays() {
    TypeToken<List<String>> type = new TypeToken<>() {};
    assertThat(gson.fromJson("[a,,b,,]", type))
        .isEqualTo(Arrays.asList("a", null, "b", null, null));
    assertThat(gson.fromJson("[,]", type)).isEqualTo(Arrays.asList(null, null));
    assertThat(gson.fromJson("[a,]", type)).isEqualTo(Arrays.asList("a", null));
  }

  @Test
  public void testExtraCommasInMaps() {
    Type type = new TypeToken<Map<String, String>>() {}.getType();
    var e = assertThrows(JsonSyntaxException.class, () -> gson.fromJson("{a:b,}", type));
    assertThat(e)
        .hasCauseThat()
        .hasMessageThat()
        .startsWith("Expected name at line 1 column 7 path $.\n");
  }
}
