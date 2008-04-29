package com.google.gson;


import java.io.PrintWriter;
import java.util.Map;

/**
 * Formats Json in a compact way eliminating all unnecessary whitespace.
 * 
 * @author Inderjeet Singh
 */
class JsonCompactFormatter implements JsonFormatter {

  private static class FormattingVisitor implements JsonElement.Visitor {
    private final PrintWriter writer;
    FormattingVisitor(PrintWriter writer) {
      this.writer = writer;
    }
    public void visit(JsonObject object) {
      writer.append('{');
      boolean first = true;
      for (Map.Entry<String, JsonElement> entry : object.getEntries()) {
        if (first) {
          first = false;
        } else {
          writer.append(',');
        }
        writer.append('"');
        writer.append(entry.getKey());
        writer.append("\":");
        entry.getValue().accept(this);
      }
      writer.append('}');
    }
    
    public void visit(JsonPrimitive primitive) {
      writer.append(primitive.toString());
    }
    
    public void visit(JsonArray array) {
      writer.append('[');
      boolean first = true;
      for (JsonElement element : array) {
        if (first) {
          first = false;
        } else {
          writer.append(',');
        }
        element.accept(this);
      }
      writer.append(']');      
    }
  }
  
  public void format(JsonElement root, PrintWriter writer) {
    if (root == null) {
      return;
    }
    root.accept(new FormattingVisitor(writer));
  }  
}
