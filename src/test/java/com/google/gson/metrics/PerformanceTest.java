package com.google.gson.metrics;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import junit.framework.TestCase;

/**
 * Tests to measure performance for Gson. All tests in this file will be disabled in code. To run
 * them remove disabled_ prefix from the tests and run them.
 * 
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class PerformanceTest extends TestCase {
  private Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();
  }
  
  public void testDummy() {    
    // This is here to prevent Junit for complaining when we disable all tests.
  }

  public void disabled_testStringDeserializationPerformance() {    
    StringBuilder sb = new StringBuilder(8096);
    sb.append("Error Yippie");

    while (true) {
      try {
        String stackTrace = sb.toString();
        sb.append(stackTrace);
        String json = "{\"message\":\"Error message.\"," + "\"stackTrace\":\"" + stackTrace + "\"}";
        parseLongJson(json);
        System.out.println("Gson could handle a string of size: " + stackTrace.length());
      } catch (JsonParseException expected) {
        break;
      }
    }
  }
  
  private void parseLongJson(String json) throws JsonParseException {
    ExceptionHolder target = gson.fromJson(json, ExceptionHolder.class);
    assertTrue(target.message.contains("Error"));
    assertTrue(target.stackTrace.contains("Yippie"));
  }

  private static class ExceptionHolder {
    public final String message;
    public final String stackTrace;
    public ExceptionHolder() {
      this("", "");
    }
    public ExceptionHolder(String message, String stackTrace) {
      this.message = message;
      this.stackTrace = stackTrace;
    }
  }
}
