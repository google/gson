package com.google.gson;


import java.io.PrintWriter;
import java.util.Map;

/**
 * Formats Json in a nicely indented way with a specified print margin. 
 * This printer tries to keep elements on the same line as much as possible 
 * while respecting right margin. 
 * 
 * @author Inderjeet Singh
 */
public class JsonPrintFormatter implements JsonFormatter {
  
  private final int printMargin;
  private final int indentationSize;
  
  static final int RIGHT_MARGIN = 4;

  public JsonPrintFormatter() {
    this(80, 2);
  }
  
  public JsonPrintFormatter(int printMargin, int indentationSize) {
    this.printMargin = printMargin;
    this.indentationSize = indentationSize;
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
      if (getLine().length() > printMargin - RIGHT_MARGIN) {
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
  
  private class PrintFormattingVisitor implements JsonElement.Visitor {
    private final JsonWriter writer;
    
    PrintFormattingVisitor(JsonWriter writer) {
      this.writer = writer;
    }
    
    public void visit(JsonObject object) {
      writer.beginObject();
      boolean first = true;
      for (Map.Entry<String, JsonElement> entry : object.getEntries()) {
        if (first) {
          first = false;
        } else {
          writer.elementSeparator();
        }
        writer.key(entry.getKey());
        writer.fieldSeparator();
        entry.getValue().accept(this);
      }
      writer.endObject();
    }

    public void visit(JsonPrimitive primitive) {
      writer.value(primitive.toString());
    }
    
    public void visit(JsonArray array) {
      writer.beginArray();
      boolean first = true;
      for (JsonElement element : array) {
        if (first) {
          first = false;
        } else {
          writer.elementSeparator();
        }
        element.accept(this);
      }
      writer.endArray();
    }
  }
  
  public void format(JsonElement root, PrintWriter writer) {
    if (root == null) {
      return;
    }
    JsonWriter jsonWriter = new JsonWriter(writer);
    PrintFormattingVisitor formattingVisitor = new PrintFormattingVisitor(jsonWriter);
    root.accept(formattingVisitor);
    jsonWriter.finishLine();
  }  
}
