// Copyright (C) 2014 Trymph Inc.
package com.google.gson.functional;

import java.io.IOException;

import junit.framework.TestCase;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("serial")
public final class ThrowableFunctionalTest extends TestCase {
  private final Gson gson = new Gson();

  public void testExceptionWithoutCause() {
    this.testThrowableWithoutCauseTemplate(RuntimeException.class, "hello", "{'detailMessage':'hello'}");
  }

  public void testExceptionWithCause() {
    Exception e = new Exception("top level", new IOException("io error"));
    String json = gson.toJson(e);
    assertTrue(json.contains("{\"detailMessage\":\"top level\",\"cause\":{\"detailMessage\":\"io error\""));

    e = gson.fromJson("{'detailMessage':'top level','cause':{'detailMessage':'io error'}}", Exception.class);
    assertEquals("top level", e.getMessage());
    assertTrue(e.getCause() instanceof Throwable); // cause is not parameterized so type info is lost
    assertEquals("io error", e.getCause().getMessage());
  }

  public void testSerializedNameOnExceptionFields() {
    MyException e = new MyException();
    String json = gson.toJson(e);
    assertTrue(json.contains("{\"my_custom_name\":\"myCustomMessageValue\""));
  }

  public void testErrorWithoutCause() {
    this.testThrowableWithoutCauseTemplate(OutOfMemoryError.class, "hello", "{'detailMessage':'hello'}");
  }

  public void testErrornWithCause() {
    Error e = new Error("top level", new IOException("io error"));
    String json = gson.toJson(e);
    assertTrue(json.contains("top level"));
    assertTrue(json.contains("io error"));

    e = gson.fromJson("{'detailMessage':'top level','cause':{'detailMessage':'io error'}}", Error.class);
    assertEquals("top level", e.getMessage());
    assertTrue(e.getCause() instanceof Throwable); // cause is not parameterized so type info is lost
    assertEquals("io error", e.getCause().getMessage());
  }

  private static final class MyException extends Throwable {
    @SerializedName("my_custom_name") String myCustomMessage = "myCustomMessageValue";
  }

  public <TThrowable extends Throwable> void testThrowableWithoutCauseTemplate(Class<TThrowable> clazzTThrowable, String msg, String jsonString) {
    try {
      TThrowable e = clazzTThrowable.getDeclaredConstructor(String.class).newInstance(msg);
      String json = gson.toJson(e);
      assertTrue(json.contains(msg));
      e = (TThrowable) gson.fromJson(jsonString, clazzTThrowable);
      assertEquals(msg, e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }
}
