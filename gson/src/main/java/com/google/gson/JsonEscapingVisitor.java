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
 * Performs JSON escaping and passes on the new escaped value to the delegate
 * {@link JsonElementVisitor}.
 *
 * @author Joel Leitch
 */
class JsonEscapingVisitor extends DelegatingJsonElementVisitor {

  /**
   * Constructs a Visitor that will properly escape any JSON primitive values.
   *
   * @param delegate the JsonElementVisitor that this instance will use for delegation
   */
  protected JsonEscapingVisitor(JsonElementVisitor delegate) {
    super(delegate);
  }

  @Override
  public void visitArrayMember(JsonArray parent, JsonPrimitive member, 
      boolean isFirst) throws IOException {
    super.visitArrayMember(parent, escapeJsonPrimitive(member), isFirst);
  }

  @Override
  public void visitObjectMember(JsonObject parent, String memberName, JsonPrimitive member, 
      boolean isFirst) throws IOException {
    super.visitObjectMember(parent, memberName, escapeJsonPrimitive(member), isFirst);
  }

  @Override
  public void visitPrimitive(JsonPrimitive primitive) throws IOException {
    super.visitPrimitive(escapeJsonPrimitive(primitive));
  }

  private JsonPrimitive escapeJsonPrimitive(JsonPrimitive member) {
    if (member.isString()) {
      String memberValue = member.getAsString();
      String escapedValue = Escaper.escapeJsonString(memberValue);
      if (!escapedValue.equals(memberValue)) {
        member.setValue(escapedValue);
      }
    }
    return member;
  }
}
