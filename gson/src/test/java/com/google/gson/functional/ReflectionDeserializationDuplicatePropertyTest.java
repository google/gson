package com.google.gson.functional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import junit.framework.TestCase;

public class ReflectionDeserializationDuplicatePropertyTest extends TestCase {
  static class MyClass {
    public String s;
  }

  public void testDuplicateProperty() {
    Gson gson = new Gson();
    String json = "{\"s\":\"1\",\"s\":\"2\"}";
    MyClass obj = gson.fromJson(json, MyClass.class);
    assertEquals("2", obj.s);
  }

  public void testDuplicatePropertyDisallowed() {
    Gson gson = new GsonBuilder().disallowDuplicatePropertyDeserialization().create();

    {
      String json = "{\"s\":\"1\",\"s\":\"2\"}";
      try {
        gson.fromJson(json, MyClass.class);
        fail();
      } catch (JsonSyntaxException e) {
        assertEquals("Duplicate property 's'", e.getMessage());
      }
    }

    {
      String json = "{\"s\":null,\"s\":null}";
      try {
        gson.fromJson(json, MyClass.class);
        fail();
      } catch (JsonSyntaxException e) {
        assertEquals("Duplicate property 's'", e.getMessage());
      }
    }

    {
      // Refers to non-existent field
      String json = "{\"other\":null,\"other\":null}";
      try {
        gson.fromJson(json, MyClass.class);
        fail();
      } catch (JsonSyntaxException e) {
        assertEquals("Duplicate property 'other'", e.getMessage());
      }
    }
  }
}
