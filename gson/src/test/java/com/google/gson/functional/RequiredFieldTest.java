package com.google.gson.functional;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Required;

import junit.framework.TestCase;

public final class RequiredFieldTest extends TestCase {
  private final Gson gson = new Gson();

  public void testExceptionIsThrownIfFieldIsAbsent() {
    try {
      gson.fromJson("{\"optional\":\"opt\"}", ClassWithRequiredField.class);
      fail("A JsonParseException should be thrown if a @Required field is absent");
    } catch (JsonParseException expected) {
    }
  }

  public void testExceptionIsThrownIfFieldIsNull() {
    try {
      gson.fromJson("{\"optional\":\"opt\",\"required\":null}", ClassWithRequiredField.class);
      fail("A JsonParseException should be thrown if a @Required field is null");
    } catch (JsonParseException expected) {
    }
  }

  public void testExceptionIsThrownIfFieldIsWrongType() {
    try {
      gson.fromJson("{\"optional\":\"opt\",\"required\":{\"test\":123}}", ClassWithRequiredField.class);
      fail("A JsonParseException should be thrown if a @Required field is of a wrong type");
    } catch (JsonParseException expected) {
    }
  }

  public void testExceptionIsNotThrownIfFieldIsPresent() {
    ClassWithRequiredField obj = gson.fromJson("{\"optional\":\"opt\",\"required\":\"reqd\"}", ClassWithRequiredField.class);
    assertEquals("reqd", obj.required);
  }

  private static final class ClassWithRequiredField {
    @Required
    public String required;
    public String optional;
  }
}
