package com.google.gson.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.gson.InstanceCreator;
import com.google.gson.ReflectionAccessFilter;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Collections;
import org.junit.Test;

public class ConstructorConstructorTest {
  private ConstructorConstructor constructorConstructor = new ConstructorConstructor(
      Collections.<Type, InstanceCreator<?>>emptyMap(), true,
      Collections.<ReflectionAccessFilter>emptyList()
  );

  private abstract static class AbstractClass {
    @SuppressWarnings("unused")
    public AbstractClass() { }
  }
  private interface Interface { }

  /**
   * Verify that ConstructorConstructor does not try to invoke no-arg constructor
   * of abstract class.
   */
  @Test
  public void testGet_AbstractClassNoArgConstructor() {
    ObjectConstructor<AbstractClass> constructor = constructorConstructor.get(TypeToken.get(AbstractClass.class));
    try {
      constructor.construct();
      fail("Expected exception");
    } catch (RuntimeException exception) {
      assertEquals(
        "Abstract classes can't be instantiated! Register an InstanceCreator or a TypeAdapter for this "
        + "type. Class name: com.google.gson.internal.ConstructorConstructorTest$AbstractClass",
        exception.getMessage()
      );
    }
  }

  @Test
  public void testGet_Interface() {
    ObjectConstructor<Interface> constructor = constructorConstructor.get(TypeToken.get(Interface.class));
    try {
      constructor.construct();
      fail("Expected exception");
    } catch (RuntimeException exception) {
      assertEquals(
        "Interfaces can't be instantiated! Register an InstanceCreator or a TypeAdapter for "
        + "this type. Interface name: com.google.gson.internal.ConstructorConstructorTest$Interface",
        exception.getMessage()
      );
    }
  }
}
