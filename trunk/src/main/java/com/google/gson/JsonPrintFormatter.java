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

import java.io.PrintWriter;

/**
 * Formats Json in a nicely indented way with a specified print margin.
 * This printer tries to keep elements on the same line as much as possible
 * while respecting right margin.
 *
 * @author Inderjeet Singh
 */
final class JsonPrintFormatter implements JsonFormatter {

  private final int printMargin;
  private final int indentationSize;
  private final int rightMargin;

  public static final int DEFAULT_PRINT_MARGIN = 80;
  public static final int DEFAULT_INDENTATION_SIZE = 2;
  public static final int DEFAULT_RIGHT_MARGIN = 4;

  public JsonPrintFormatter() {
    this(DEFAULT_PRINT_MARGIN, DEFAULT_INDENTATION_SIZE, DEFAULT_RIGHT_MARGIN);
  }

  public JsonPrintFormatter(int printMargin, int indentationSize, int rightMargin) {
    this.printMargin = printMargin;
    this.indentationSize = indentationSize;
    this.rightMargin = rightMargin;
  }

  private class JsonWriter {
    private final PrintWriter writer;
    private StringBuilder line;
    private int level;
    JsonWriter(PrintWriter writer) {
      this.writer = writer;
      level = 0;
      line = new StringBuilder();
    }

    void key(String key) {
      getLine().append('"');
      getLine().append(key);
      getLine().append('"');
    }

    void value(String value) {
      getLine().append(value);
    }

    void fieldSeparator() {
      getLine().append(':');
      breakLineIfNeeded();
    }

    void elementSeparator() {
      getLine().append(',');
      breakLineIfNeeded();
    }

    void beginObject() {
      ++level;
      breakLineIfNeeded();
      getLine().append('{');
    }

    void endObject() {
      getLine().append('}');
      --level;
    }

    void beginArray() {
      ++level;
      breakLineIfNeeded();
      getLine().append('[');
    }

    void endArray() {
      getLine().append(']');
      --level;
    }

    private void breakLineIfNeeded() {
      if (getLine().length() > printMargin - rightMargin) {
        finishLine();
      }
    }

    void finishLine() {
      if (line != null) {
        writer.append(line).append("\n");
      }
      line = null;
    }

    private StringBuilder getLine() {
      if (line == null) {
        createNewLine();
      }
      return line;
    }

    private void createNewLine() {
      line = new StringBuilder();
      for (int i = 0; i < level; ++i) {
        for (int j = 0; j < indentationSize; ++j) {
          line.append(' ');
        }
      }
    }
  }

  private class PrintFormattingVisitor implements JsonElementVisitor {
    private final JsonWriter writer;
    private boolean first;

    PrintFormattingVisitor(JsonWriter writer) {
      this.writer = writer;
    }

    private void addCommaCheckingFirst() {
      if (first) {
        first = false;
      } else {
        writer.elementSeparator();
      }
    }

    public void startArray(JsonArray array) {
      writer.beginArray();
    }

    public void visitArrayMember(JsonArray parent, JsonPrimitive member, boolean isFirst) {
      addCommaCheckingFirst();
      writer.value(member.toString());
    }

    public void visitArrayMember(JsonArray parent, JsonArray member, boolean first) {
      addCommaCheckingFirst();
    }

    public void visitArrayMember(JsonArray parent, JsonObject member, boolean first) {
      addCommaCheckingFirst();
    }

    public void endArray(JsonArray array) {
      writer.endArray();
    }

    public void startObject(JsonObject object) {
      writer.beginObject();
    }

    public void visitObjectMember(JsonObject parent, String memberName, JsonPrimitive member, boolean isFirst) {
      addCommaCheckingFirst();
      writer.key(memberName);
      writer.fieldSeparator();
      writer.value(member.toString());
    }

    public void visitObjectMember(JsonObject parent, String memberName, JsonArray member, boolean isFirst) {
      addCommaCheckingFirst();
      writer.key(memberName);
      writer.fieldSeparator();
    }

    public void visitObjectMember(JsonObject parent, String memberName, JsonObject member, boolean isFirst) {
      addCommaCheckingFirst();
      writer.key(memberName);
      writer.fieldSeparator();
    }

    public void endObject(JsonObject object) {
      writer.endObject();
    }

    public void visitPrimitive(JsonPrimitive primitive) {
      writer.value(primitive.toString());
    }
  }

  public void format(JsonElement root, PrintWriter writer) {
    if (root == null) {
      return;
    }
    JsonWriter jsonWriter = new JsonWriter(writer);
    JsonElementVisitor visitor = new JsonEscapingVisitor(new PrintFormattingVisitor(jsonWriter));
    JsonTreeNavigator navigator = new JsonTreeNavigator(visitor);
    navigator.navigate(root);
    jsonWriter.finishLine();
  }
}
