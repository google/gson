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

import junit.framework.TestCase;

/**
 * Performs some unit testing for the {@link JsonEscapingVisitor} class.
 *
 * @author Joel Leitch
 */
public class JsonEscapingVisitorTest extends TestCase {
  private StubbedJsonElementVisitor stubVisitor;
  private JsonEscapingVisitor escapingVisitor;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    stubVisitor = new StubbedJsonElementVisitor();
    escapingVisitor = new JsonEscapingVisitor(stubVisitor);
  }

  public void testNonStringPrimitiveVisitation() throws Exception {
    boolean value = true;
    JsonPrimitive primitive = new JsonPrimitive(value);
    escapingVisitor.visitPrimitive(primitive);
    assertEquals(value, stubVisitor.primitiveReceived.getAsBoolean());
  }

  public void testStringPrimitiveVisitationNoEscapingRequired() throws Exception {
    String value = "Testing123";
    JsonPrimitive primitive = new JsonPrimitive(value);
    escapingVisitor.visitPrimitive(primitive);
    assertEquals(value, stubVisitor.primitiveReceived.getAsObject());
  }

  public void testStringPrimitiveVisitationEscapingRequired() throws Exception {
    String value = "Testing\"123";
    JsonPrimitive primitive = new JsonPrimitive(value);
    escapingVisitor.visitPrimitive(primitive);
    assertEquals(Escaper.escapeJsonString(value), stubVisitor.primitiveReceived.getAsString());
  }

  public void testNonStringArrayVisitation() throws Exception {
    int value = 123;
    JsonPrimitive primitive = new JsonPrimitive(value);
    JsonArray array = new JsonArray();
    array.add(primitive);
    escapingVisitor.visitArrayMember(array, primitive, true);
    assertEquals(value, stubVisitor.primitiveReceived.getAsInt());
  }

  public void testStringArrayVisitationNoEscaping() throws Exception {
    String value = "Testing123";
    JsonPrimitive primitive = new JsonPrimitive(value);
    JsonArray array = new JsonArray();
    array.add(primitive);
    escapingVisitor.visitArrayMember(array, primitive, true);
    assertEquals(value, stubVisitor.primitiveReceived.getAsString());
  }

  public void testStringArrayVisitationEscapingRequired() throws Exception {
    String value = "Testing\"123";
    JsonPrimitive primitive = new JsonPrimitive(value);
    JsonArray array = new JsonArray();
    array.add(primitive);
    escapingVisitor.visitArrayMember(array, primitive, true);
    assertEquals(Escaper.escapeJsonString(value), stubVisitor.primitiveReceived.getAsString());
  }

  public void testNonStringFieldVisitation() throws Exception {
    String fieldName = "fieldName";
    int value = 123;
    JsonPrimitive primitive = new JsonPrimitive(value);
    JsonObject object = new JsonObject();
    object.addProperty(fieldName, value);

    escapingVisitor.visitObjectMember(object, fieldName, primitive, true);
    assertEquals(value, stubVisitor.primitiveReceived.getAsInt());
  }

  public void testStringFieldVisitationNoEscaping() throws Exception {
    String fieldName = "fieldName";
    String value = "Testing123";
    JsonPrimitive primitive = new JsonPrimitive(value);
    JsonObject object = new JsonObject();
    object.addProperty(fieldName, value);

    escapingVisitor.visitObjectMember(object, fieldName, primitive, true);
    assertEquals(value, stubVisitor.primitiveReceived.getAsString());
  }

  public void testStringFieldVisitationEscapingRequired() throws Exception {
    String fieldName = "fieldName";
    String value = "Testing\"123";
    JsonPrimitive primitive = new JsonPrimitive(value);
    JsonObject object = new JsonObject();
    object.addProperty(fieldName, value);

    escapingVisitor.visitObjectMember(object, fieldName, primitive, true);
    assertEquals(Escaper.escapeJsonString(value), stubVisitor.primitiveReceived.getAsString());
  }

  private static class StubbedJsonElementVisitor implements JsonElementVisitor {
    public JsonPrimitive primitiveReceived;

    public void endArray(JsonArray array) {
      // Do nothing
    }

    public void endObject(JsonObject object) {
      // Do nothing
    }

    public void startArray(JsonArray array) {
      // Do nothing
    }

    public void startObject(JsonObject object) {
      // Do nothing
    }

    public void visitArrayMember(JsonArray parent, JsonPrimitive member, boolean isFirst) {
      primitiveReceived = member;
    }

    public void visitArrayMember(JsonArray parent, JsonArray member, boolean isFirst) {
      // Do nothing
    }

    public void visitArrayMember(JsonArray parent, JsonObject member, boolean isFirst) {
      // Do nothing
    }

    public void visitObjectMember(
        JsonObject parent, String memberName, JsonPrimitive member, boolean isFirst) {
      primitiveReceived = member;
    }

    public void visitObjectMember(
        JsonObject parent, String memberName, JsonArray member, boolean isFirst) {
      // Do nothing
    }

    public void visitObjectMember(
        JsonObject parent, String memberName, JsonObject member, boolean isFirst) {
      // Do nothing
    }

    public void visitPrimitive(JsonPrimitive primitive) {
      primitiveReceived = primitive;
    }

    public void visitNullArrayMember(JsonArray parent, boolean isFirst) {
      // Do nothing
    }

    public void visitNull() {
      // Do nothing      
    }

    public void visitNullObjectMember(JsonObject parent, String memberName, boolean isFirst) {
      // Do nothing      
    }
  }
}
