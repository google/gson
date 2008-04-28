package com.google.gson;

import java.io.PrintWriter;

/**
 * Common interface for a formatter for Json. 
 * 
 * @author Inderjeet Singh
 */
public interface JsonFormatter {

  /**
   * Writes a formatted version of the Json corresponding to 
   * the specified Json.  
   * 
   * @param root the root of the Json tree. 
   */
  public void format(JsonElement root, PrintWriter writer);
}
