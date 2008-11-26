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

import java.io.IOException;

/**
 * A simple implementation of the {@link JsonElementVisitor} that simply delegates the method
 * invocation onto a {@code delegate} instance of the {@link JsonElementVisitor}.  This object
 * can be used to build a chain of visitors such that each Visitor instance can perform some
 * operation on the {@link JsonElement} and then pass on the input to the delegate.  This kind
 * of pattern is sometimes referred as a "Chain of Responsibility".
 *
 * <p>The following is an example use case:
 *
 * <pre>
 * class JsonEscapingVisitor extends DelegatingJsonElementVisitor {
 *   public JsonEscapingVisitor(JsonElementVisitor) {
 *     super(visitor);
 *   }
 *
 *   public void visitPrimitive(JsonPrimitive primitive) {
 *     JsonPrimitive escapedPrimitive = escapePrimitiveObject(primitive);
 *     super.visitPrimitive(escapedPrimitive);
 *   }
 * }
 *
 * JsonElementVisitor visitor = new JsonEscapingVisitor(new FormattingVisitor());
 * </pre></p>
 *
 * @author Joel Leitch
 */
class DelegatingJsonElementVisitor implements JsonElementVisitor {
  private final JsonElementVisitor delegate;

  protected DelegatingJsonElementVisitor(JsonElementVisitor delegate) {
    Preconditions.checkNotNull(delegate);
    this.delegate = delegate;
  }

  public void endArray(JsonArray array) throws IOException {
    delegate.endArray(array);
  }

  public void endObject(JsonObject object) throws IOException {
    delegate.endObject(object);
  }

  public void startArray(JsonArray array) throws IOException {
    delegate.startArray(array);
  }

  public void startObject(JsonObject object) throws IOException {
    delegate.startObject(object);
  }

  public void visitArrayMember(JsonArray parent, JsonPrimitive member, 
      boolean isFirst) throws IOException {
    delegate.visitArrayMember(parent, member, isFirst);
  }

  public void visitArrayMember(JsonArray parent, JsonArray member, 
      boolean isFirst) throws IOException {
    delegate.visitArrayMember(parent, member, isFirst);
  }

  public void visitArrayMember(JsonArray parent, JsonObject member, 
      boolean isFirst) throws IOException {
    delegate.visitArrayMember(parent, member, isFirst);
  }

  public void visitObjectMember(JsonObject parent, String memberName, JsonPrimitive member, 
      boolean isFirst) throws IOException {
    delegate.visitObjectMember(parent, memberName, member, isFirst);
  }

  public void visitObjectMember(JsonObject parent, String memberName, JsonArray member, 
      boolean isFirst) throws IOException {
    delegate.visitObjectMember(parent, memberName, member, isFirst);
  }

  public void visitObjectMember(JsonObject parent, String memberName, JsonObject member, 
      boolean isFirst) throws IOException {
    delegate.visitObjectMember(parent, memberName, member, isFirst);
  }

  public void visitNullObjectMember(JsonObject parent, String memberName, 
      boolean isFirst) throws IOException {
    delegate.visitNullObjectMember(parent, memberName, isFirst);
  }

  public void visitPrimitive(JsonPrimitive primitive) throws IOException {
    delegate.visitPrimitive(primitive);
  }

  public void visitNull() throws IOException {
    delegate.visitNull();
  }

  public void visitNullArrayMember(JsonArray parent, boolean isFirst) throws IOException {
    delegate.visitNullArrayMember(parent, isFirst);
  }
}
