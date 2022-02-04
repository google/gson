package com.google.gson.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.ReflectionAccessFilter;
import com.google.gson.ReflectionAccessFilter.FilterResult;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.awt.Point;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;

public class ReflectionAccessFilterTest {
  @Test
  public void testBlockInaccessibleJava() {
    Gson gson = new GsonBuilder()
      .addReflectionAccessFilter(ReflectionAccessFilter.BLOCK_INACCESSIBLE_JAVA)
      .create();

    // Serialization should fail for classes with non-public fields
    try {
      gson.toJson(Thread.currentThread());
      fail("Expected exception; test needs to be run with Java >= 9");
    } catch (JsonIOException expected) {
      assertEquals(
        "Field 'java.lang.Thread#name' is not accessible and ReflectionAccessFilter does not permit "
        + "making it accessible. Register a TypeAdapter for the declaring type or adjust the access filter.",
        expected.getMessage()
      );
    }

    // But serialization should succeed for classes with only public fields
    String json = gson.toJson(new Point(1, 2));
    assertEquals("{\"x\":1,\"y\":2}", json);
  }

  @Test
  public void testBlockAllJava() {
    Gson gson = new GsonBuilder()
      .addReflectionAccessFilter(ReflectionAccessFilter.BLOCK_ALL_JAVA)
      .create();

    // Serialization should fail for any Java class
    try {
      gson.toJson(Thread.currentThread());
      fail();
    } catch (JsonIOException expected) {
      assertEquals(
        "ReflectionAccessFilter does not permit using reflection for class java.lang.Thread. "
        + "Register a TypeAdapter for this type or adjust the access filter.",
        expected.getMessage()
      );
    }
  }

  private static class SuperTestClass {
    @SuppressWarnings("unused")
    public int i = 1;
  }
  private static class SubTestClass extends SuperTestClass {
  }
  private static class OtherClass {
    @SuppressWarnings("unused")
    public int i = 2;
  }

  @Test
  public void testDelegation() {
    Gson gson = new GsonBuilder()
      .addReflectionAccessFilter(new ReflectionAccessFilter() {
        @Override public FilterResult check(Class<?> rawClass) {
          // INDECISIVE in last filter should act like ALLOW
          return SuperTestClass.class.isAssignableFrom(rawClass) ? FilterResult.BLOCK_ALL : FilterResult.INDECISIVE;
        }
      })
      .addReflectionAccessFilter(new ReflectionAccessFilter() {
        @Override public FilterResult check(Class<?> rawClass) {
          // INDECISIVE should delegate to previous filter
          return rawClass == SubTestClass.class ? FilterResult.ALLOW : FilterResult.INDECISIVE;
        }
      })
      .create();

    // Filter disallows SuperTestClass
    try {
      gson.toJson(new SuperTestClass());
      fail();
    } catch (JsonIOException expected) {
      assertEquals(
        "ReflectionAccessFilter does not permit using reflection for class "
        + "com.google.gson.functional.ReflectionAccessFilterTest$SuperTestClass. "
        + "Register a TypeAdapter for this type or adjust the access filter.",
        expected.getMessage()
      );
    }

    // But registration order is reversed, so filter for SubTestClass allows reflection
    String json = gson.toJson(new SubTestClass());
    assertEquals("{\"i\":1}", json);

    // And unrelated class should not be affected
    json = gson.toJson(new OtherClass());
    assertEquals("{\"i\":2}", json);
  }

  @Test
  public void testInaccessibleNoArgsConstructor() {
    Gson gson = new GsonBuilder()
      .addReflectionAccessFilter(new ReflectionAccessFilter() {
        @Override public FilterResult check(Class<?> rawClass) {
          return FilterResult.BLOCK_INACCESSIBLE;
        }
      })
      .create();

    try {
      // Class Runtime has private no-args constructor
      gson.fromJson("{}", Runtime.class);
      fail("Expected exception; test needs to be run with Java >= 9");
    } catch (JsonIOException expected) {
      assertEquals(
        "Unable to invoke no-args constructor of class java.lang.Runtime; constructor is not accessible "
        + "and ReflectionAccessFilter does not permit making it accessible. Register an InstanceCreator "
        + "or a TypeAdapter for this type, change the visibility of the constructor or adjust the access filter.",
        expected.getMessage()
      );
    }
  }

  private static class ClassWithoutNoArgsConstructor {
    public String s;

    public ClassWithoutNoArgsConstructor(String s) {
      this.s = s;
    }
  }

  @Test
  public void testClassWithoutNoArgsConstructor() {
    GsonBuilder gsonBuilder = new GsonBuilder()
      .addReflectionAccessFilter(new ReflectionAccessFilter() {
        @Override public FilterResult check(Class<?> rawClass) {
          // Even BLOCK_INACCESSIBLE should prevent usage of Unsafe for object creation
          return FilterResult.BLOCK_INACCESSIBLE;
        }
      });
    Gson gson = gsonBuilder.create();

    try {
      gson.fromJson("{}", ClassWithoutNoArgsConstructor.class);
      fail();
    } catch (JsonIOException expected) {
      assertEquals(
        "Unable to create instance of class com.google.gson.functional.ReflectionAccessFilterTest$ClassWithoutNoArgsConstructor; "
        + "ReflectionAccessFilter does not permit using reflection or Unsafe. Register an InstanceCreator "
        + "or a TypeAdapter for this type or adjust the access filter to allow using reflection.",
        expected.getMessage()
      );
    }

    // But should not fail when custom TypeAdapter is specified
    gson = gson.newBuilder()
      .registerTypeAdapter(ClassWithoutNoArgsConstructor.class, new TypeAdapter<ClassWithoutNoArgsConstructor>() {
        @Override public ClassWithoutNoArgsConstructor read(JsonReader in) throws IOException {
          in.skipValue();
          return new ClassWithoutNoArgsConstructor("TypeAdapter");
        }
        @Override public void write(JsonWriter out, ClassWithoutNoArgsConstructor value) throws IOException {
          throw new AssertionError("Not needed for test");
        };
      })
      .create();
    ClassWithoutNoArgsConstructor deserialized = gson.fromJson("{}", ClassWithoutNoArgsConstructor.class);
    assertEquals("TypeAdapter", deserialized.s);

    // But should not fail when custom InstanceCreator is specified
    gson = gsonBuilder
      .registerTypeAdapter(ClassWithoutNoArgsConstructor.class, new InstanceCreator<ClassWithoutNoArgsConstructor>() {
        @Override public ClassWithoutNoArgsConstructor createInstance(Type type) {
          return new ClassWithoutNoArgsConstructor("InstanceCreator");
        }
      })
      .create();
    deserialized = gson.fromJson("{}", ClassWithoutNoArgsConstructor.class);
    assertEquals("InstanceCreator", deserialized.s);
  }

  /**
   * When using {@link FilterResult#BLOCK_ALL}, registering only a {@link JsonSerializer}
   * but not performing any deserialization should not throw any exception.
   */
  @Test
  public void testBlockAllPartial() {
    Gson gson = new GsonBuilder()
      .addReflectionAccessFilter(new ReflectionAccessFilter() {
        @Override public FilterResult check(Class<?> rawClass) {
          return FilterResult.BLOCK_ALL;
        }
      })
      .registerTypeAdapter(OtherClass.class, new JsonSerializer<OtherClass>() {
        @Override public JsonElement serialize(OtherClass src, Type typeOfSrc, JsonSerializationContext context) {
          return new JsonPrimitive(123);
        }
      })
      .create();

    String json = gson.toJson(new OtherClass());
    assertEquals("123", json);

    // But deserialization should fail
    try {
      gson.fromJson("{}", OtherClass.class);
      fail();
    } catch (JsonIOException expected) {
      assertEquals(
        "ReflectionAccessFilter does not permit using reflection for class com.google.gson.functional.ReflectionAccessFilterTest$OtherClass. "
        + "Register a TypeAdapter for this type or adjust the access filter.",
        expected.getMessage()
      );
    }
  }

  /**
   * Should not fail when deserializing collection interface
   * (Even though this goes through {@link ConstructorConstructor} as well)
   */
  @Test
  public void testBlockAllCollectionInterface() {
    Gson gson = new GsonBuilder()
      .addReflectionAccessFilter(new ReflectionAccessFilter() {
        @Override public FilterResult check(Class<?> rawClass) {
          return FilterResult.BLOCK_ALL;
        }
      })
      .create();
    List<?> deserialized = gson.fromJson("[1.0]", List.class);
    assertEquals(1.0, deserialized.get(0));
  }

  /**
   * Should not fail when deserializing specific collection implementation
   * (Even though this goes through {@link ConstructorConstructor} as well)
   */
  @Test
  public void testBlockAllCollectionImplementation() {
    Gson gson = new GsonBuilder()
      .addReflectionAccessFilter(new ReflectionAccessFilter() {
        @Override public FilterResult check(Class<?> rawClass) {
          return FilterResult.BLOCK_ALL;
        }
      })
      .create();
    List<?> deserialized = gson.fromJson("[1.0]", LinkedList.class);
    assertEquals(1.0, deserialized.get(0));
  }

  /**
   * When trying to deserialize interface an exception for that should
   * be thrown, even if {@link FilterResult#BLOCK_INACCESSIBLE} is used
   */
  @Test
  public void testBlockInaccessibleInterface() {
    Gson gson = new GsonBuilder()
      .addReflectionAccessFilter(new ReflectionAccessFilter() {
        @Override public FilterResult check(Class<?> rawClass) {
          return FilterResult.BLOCK_INACCESSIBLE;
        }
      })
      .create();

    try {
      gson.fromJson("{}", Runnable.class);
      fail();
    } catch (JsonIOException expected) {
      assertEquals(
        "Interfaces can't be instantiated! Register an InstanceCreator or a TypeAdapter for "
        + "this type. Interface name: java.lang.Runnable",
        expected.getMessage()
      );
    }
  }
}
