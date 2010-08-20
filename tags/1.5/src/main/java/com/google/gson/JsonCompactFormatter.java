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
 * Formats Json in a compact way eliminating all unnecessary whitespace.
 *
 * @author Inderjeet Singh
 */
final class JsonCompactFormatter implements JsonFormatter {

  private static class FormattingVisitor implements JsonElementVisitor {
    private final Appendable writer;
    private final Escaper escaper;
    private final boolean serializeNulls;

    FormattingVisitor(Appendable writer, Escaper escaper, boolean serializeNulls) {
      this.writer = writer;
      this.escaper = escaper;
      this.serializeNulls = serializeNulls;
    }

    public void visitPrimitive(JsonPrimitive primitive) throws IOException {
      primitive.toString(writer, escaper);
    }

    public void visitNull() throws IOException {
      writer.append("null");
    }
    
    public void startArray(JsonArray array) throws IOException {
      writer.append('[');
    }

    public void visitArrayMember(JsonArray parent, JsonPrimitive member, 
        boolean isFirst) throws IOException {
      if (!isFirst) {
        writer.append(',');
      }
      member.toString(writer, escaper);
    }

    public void visitArrayMember(JsonArray parent, JsonArray member, 
        boolean isFirst) throws IOException {
      if (!isFirst) {
        writer.append(',');
      }
    }

    public void visitArrayMember(JsonArray parent, JsonObject member, 
        boolean isFirst) throws IOException {
      if (!isFirst) {
        writer.append(',');
      }
    }

    public void visitNullArrayMember(JsonArray parent, boolean isFirst) throws IOException {
      if (!isFirst) {
        writer.append(',');
      }
    }

    public void endArray(JsonArray array) throws IOException {
      writer.append(']');
    }

    public void startObject(JsonObject object) throws IOException {
      writer.append('{');
    }

    public void visitObjectMember(JsonObject parent, String memberName, JsonPrimitive member,
        boolean isFirst) throws IOException {
      if (!isFirst) {
        writer.append(',');
      }
      writer.append('"');
      writer.append(memberName);
      writer.append("\":");
      member.toString(writer, escaper);
    }

    public void visitObjectMember(JsonObject parent, String memberName, JsonArray member,
        boolean isFirst) throws IOException {
      if (!isFirst) {
        writer.append(',');
      }
      writer.append('"');
      writer.append(memberName);
      writer.append("\":");
    }

    public void visitObjectMember(JsonObject parent, String memberName, JsonObject member,
        boolean isFirst) throws IOException {
      if (!isFirst) {
        writer.append(',');
      }
      writer.append('"');
      writer.append(memberName);
      writer.append("\":");
    }

    public void visitNullObjectMember(JsonObject parent, String memberName, 
        boolean isFirst) throws IOException {
      if (serializeNulls) {
        visitObjectMember(parent, memberName, (JsonObject) null, isFirst);
      }      
    }
    
    public void endObject(JsonObject object) throws IOException {
      writer.append('}');
    }
  }
  
  private final boolean escapeHtmlChars;

  JsonCompactFormatter() {
    this(true);
  }
  
  JsonCompactFormatter(boolean escapeHtmlChars) {
    this.escapeHtmlChars = escapeHtmlChars;
  }

  public void format(JsonElement root, Appendable writer, 
      boolean serializeNulls) throws IOException {
    if (root == null) {
      return;
    }
    JsonElementVisitor visitor = new FormattingVisitor(
        writer, new Escaper(escapeHtmlChars), serializeNulls);
    JsonTreeNavigator navigator = new JsonTreeNavigator(visitor, serializeNulls);
    navigator.navigate(root);
  }
}
