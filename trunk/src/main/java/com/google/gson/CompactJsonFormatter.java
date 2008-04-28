package com.google.gson;

import java.io.PrintWriter;

/**
 * Formats Json in a compact way eliminating all unnecessary whitespace.
 * 
 * @author Inderjeet Singh
 */
class CompactJsonFormatter implements JsonFormatter {

  public void format(JsonElement root, PrintWriter writer) {
    if (root == null) {
      return;
    }
    
    // Since this formatter is used only from Gson, and Gson already
    // formats its string compactly, we dont need to do anything.
    // If the implementation of Gson changes, or this class is made
    // public, we will need to reimplement this method to eliminate
    // all redundant whitespace characters.
    
    writer.append(root.toJson());
  }
}
