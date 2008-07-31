package com.google.gson;

import java.lang.reflect.Type;

import junit.framework.TestCase;

import com.google.gson.TestTypes.BagOfPrimitives;
import com.google.gson.TestTypes.ClassWithObjects;

public class NullSerializationTests extends TestCase {
  private GsonBuilder gsonBuilder;

  @Override
  protected void setUp() throws Exception {
    super.setUp();    
    gsonBuilder = new GsonBuilder().serializeNulls();
  }
  
  public void testExplicitSerializationOfNulls() {
    Gson gson = gsonBuilder.create();
    ClassWithObjects target = new ClassWithObjects(null);
    String actual = gson.toJson(target);
    String expected = "{\"bag\":null}";
    assertEquals(expected, actual);
  }
  
  private static class ClassWithObjectsSerializer implements JsonSerializer<ClassWithObjects> {
    public JsonElement serialize(ClassWithObjects src, Type typeOfSrc, 
        JsonSerializationContext context) {
      JsonObject obj = new JsonObject();
      obj.add("bag", new JsonNull());
      return obj;
    }    
  }
  
  public void testCustomSerializationOfNulls() {
    gsonBuilder.registerSerializer(ClassWithObjects.class, new ClassWithObjectsSerializer());
    Gson gson = gsonBuilder.create();
    ClassWithObjects target = new ClassWithObjects(new BagOfPrimitives());
    String actual = gson.toJson(target);
    String expected = "{\"bag\":null}";
    assertEquals(expected, actual);
  }
}
