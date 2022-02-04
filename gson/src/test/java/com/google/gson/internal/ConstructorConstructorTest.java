package com.google.gson.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.Type;
import java.util.Collections;

import org.junit.Test;

import com.google.gson.InstanceCreator;
import com.google.gson.JsonIOException;
import com.google.gson.ReflectionAccessFilter;
import com.google.gson.reflect.TypeToken;

public class ConstructorConstructorTest {
  private ConstructorConstructor constructorConstructor = new ConstructorConstructor(
      Collections.<Type, InstanceCreator<?>>emptyMap(), true,
      Collections.<ReflectionAccessFilter>emptyList()
  );

  public interface Interface {
  }

  public static abstract class AbstractClass {
    // Add constructor with arguments to work around https://github.com/google/gson/pull/1814
    AbstractClass(int i) { }
  }

  @Test
  public void testConstructInterface() throws Exception {
    try {
      constructorConstructor.get(TypeToken.get(Interface.class)).construct();
      fail();
    } catch (JsonIOException expected) {
      assertEquals(
        "Interfaces can't be instantiated! Register an InstanceCreator or a TypeAdapter for this "
        + "type. Interface name: com.google.gson.internal.ConstructorConstructorTest$Interface",
        expected.getMessage()
      );
    }
  }

  @Test
  public void testConstructAbstractClass() throws Exception {
    try {
      constructorConstructor.get(TypeToken.get(AbstractClass.class)).construct();
      fail();
    } catch (JsonIOException expected) {
      assertEquals(
        "Abstract classes can't be instantiated! Register an InstanceCreator or a TypeAdapter for "
        + "this type. Class name: com.google.gson.internal.ConstructorConstructorTest$AbstractClass",
        expected.getMessage()
      );
    }
  }
}
