package com.google.gson.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.gson.InstanceCreator;
import com.google.gson.ReflectionAccessFilter;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.LinkedBlockingDeque;
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

  @SuppressWarnings("serial")
  private static class CustomSortedSet<E> extends TreeSet<E> {
    // Removes default no-args constructor
    @SuppressWarnings("unused")
    CustomSortedSet(Void v) {
    }
  }

  @SuppressWarnings("serial")
  private static class CustomSet<E> extends HashSet<E> {
    // Removes default no-args constructor
    @SuppressWarnings("unused")
    CustomSet(Void v) {
    }
  }

  @SuppressWarnings("serial")
  private static class CustomQueue<E> extends LinkedBlockingDeque<E> {
    // Removes default no-args constructor
    @SuppressWarnings("unused")
    CustomQueue(Void v) {
    }
  }

  @SuppressWarnings("serial")
  private static class CustomList<E> extends ArrayList<E> {
    // Removes default no-args constructor
    @SuppressWarnings("unused")
    CustomList(Void v) {
    }
  }

  /**
   * Tests that creation of custom {@code Collection} subclasses without no-args constructor
   * should not use default JDK types (which would cause {@link ClassCastException}).
   *
   * <p>Currently this test is rather contrived because the instances created using
   * Unsafe are not usable because their fields are not properly initialized, but
   * assume that user has custom classes which would be functional.
   */
  @Test
  public void testCustomCollectionCreation() {
    Class<?>[] collectionTypes = {
        CustomSortedSet.class,
        CustomSet.class,
        CustomQueue.class,
        CustomList.class,
    };

    for (Class<?> collectionType : collectionTypes) {
      Object actual = constructorConstructor.get(TypeToken.getParameterized(collectionType, Integer.class)).construct();
      assertTrue("Failed for " + collectionType + "; created instance of " + actual.getClass(), collectionType.isInstance(actual));
    }
  }

  private static interface CustomCollectionInterface extends Collection<String> {
  }
  private static interface CustomSetInterface extends Set<String> {
  }
  private static interface CustomListInterface extends List<String> {
  }

  @Test
  public void testCustomCollectionInterfaceCreation() {
    Class<?>[] interfaces = {
        CustomCollectionInterface.class,
        CustomSetInterface.class,
        CustomListInterface.class,
    };

    for (Class<?> interfaceType : interfaces) {
      try {
        constructorConstructor.get(TypeToken.get(interfaceType)).construct();
        fail();
      } catch (RuntimeException e) {
        assertEquals("Interfaces can't be instantiated! Register an InstanceCreator or a TypeAdapter"
            + " for this type. Interface name: " + interfaceType.getName(), e.getMessage());
      }
    }
  }

  private enum MyEnum {
  }

  @SuppressWarnings("serial")
  private static class CustomEnumMap<K, V> extends EnumMap<MyEnum, V> {
    @SuppressWarnings("unused")
    CustomEnumMap(Void v) {
      super(MyEnum.class);
    }
  }

  @SuppressWarnings("serial")
  private static class CustomConcurrentNavigableMap<K, V> extends ConcurrentSkipListMap<K, V> {
    // Removes default no-args constructor
    @SuppressWarnings("unused")
    CustomConcurrentNavigableMap(Void v) {
    }
  }

  @SuppressWarnings("serial")
  private static class CustomConcurrentMap<K, V> extends ConcurrentHashMap<K, V> {
    // Removes default no-args constructor
    @SuppressWarnings("unused")
    CustomConcurrentMap(Void v) {
    }
  }

  @SuppressWarnings("serial")
  private static class CustomSortedMap<K, V> extends TreeMap<K, V> {
    // Removes default no-args constructor
    @SuppressWarnings("unused")
    CustomSortedMap(Void v) {
    }
  }

  @SuppressWarnings("serial")
  private static class CustomLinkedHashMap<K, V> extends LinkedHashMap<K, V> {
    // Removes default no-args constructor
    @SuppressWarnings("unused")
    CustomLinkedHashMap(Void v) {
    }
  }

  /**
   * Tests that creation of custom {@code Map} subclasses without no-args constructor
   * should not use default JDK types (which would cause {@link ClassCastException}).
   *
   * <p>Currently this test is rather contrived because the instances created using
   * Unsafe are not usable because their fields are not properly initialized, but
   * assume that user has custom classes which would be functional.
   */
  @Test
  public void testCustomMapCreation() {
    Class<?>[] mapTypes = {
        CustomEnumMap.class,
        CustomConcurrentNavigableMap.class,
        CustomConcurrentMap.class,
        CustomSortedMap.class,
        CustomLinkedHashMap.class,
    };

    for (Class<?> mapType : mapTypes) {
      Object actual = constructorConstructor.get(TypeToken.getParameterized(mapType, String.class, Integer.class)).construct();
      assertTrue("Failed for " + mapType + "; created instance of " + actual.getClass(), mapType.isInstance(actual));
    }
  }

  private static interface CustomMapInterface extends Map<String, Integer> {
  }

  @Test
  public void testCustomMapInterfaceCreation() {
    try {
      constructorConstructor.get(TypeToken.get(CustomMapInterface.class)).construct();
      fail();
    } catch (RuntimeException e) {
      assertEquals("Interfaces can't be instantiated! Register an InstanceCreator or a TypeAdapter"
          + " for this type. Interface name: " + CustomMapInterface.class.getName(), e.getMessage());
    }
  }
}
