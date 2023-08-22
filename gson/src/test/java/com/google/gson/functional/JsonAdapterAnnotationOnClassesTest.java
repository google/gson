/*
 * Copyright (C) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.functional;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.common.base.Splitter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import org.junit.Test;

/**
 * Functional tests for the {@link JsonAdapter} annotation on classes.
 */
public final class JsonAdapterAnnotationOnClassesTest {

  @Test
  public void testJsonAdapterInvoked() {
    Gson gson = new Gson();
    String json = gson.toJson(new A("bar"));
    assertThat(json).isEqualTo("\"jsonAdapter\"");

   // Also invoke the JsonAdapter javadoc sample
    json = gson.toJson(new User("Inderjeet", "Singh"));
    assertThat(json).isEqualTo("{\"name\":\"Inderjeet Singh\"}");
    User user = gson.fromJson("{'name':'Joel Leitch'}", User.class);
    assertThat(user.firstName).isEqualTo("Joel");
    assertThat(user.lastName).isEqualTo("Leitch");

    json = gson.toJson(Foo.BAR);
    assertThat(json).isEqualTo("\"bar\"");
    Foo baz = gson.fromJson("\"baz\"", Foo.class);
    assertThat(baz).isEqualTo(Foo.BAZ);
  }

  @Test
  public void testJsonAdapterFactoryInvoked() {
    Gson gson = new Gson();
    String json = gson.toJson(new C("bar"));
    assertThat(json).isEqualTo("\"jsonAdapterFactory\"");
    C c = gson.fromJson("\"bar\"", C.class);
    assertThat(c.value).isEqualTo("jsonAdapterFactory");
  }

  @Test
  public void testRegisteredAdapterOverridesJsonAdapter() {
    TypeAdapter<A> typeAdapter = new TypeAdapter<A>() {
      @Override public void write(JsonWriter out, A value) throws IOException {
        out.value("registeredAdapter");
      }
      @Override public A read(JsonReader in) throws IOException {
        return new A(in.nextString());
      }
    };
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(A.class, typeAdapter)
      .create();
    String json = gson.toJson(new A("abcd"));
    assertThat(json).isEqualTo("\"registeredAdapter\"");
  }

  /**
   * The serializer overrides field adapter, but for deserializer the fieldAdapter is used.
   */
  @Test
  public void testRegisteredSerializerOverridesJsonAdapter() {
    JsonSerializer<A> serializer = new JsonSerializer<A>() {
      @Override public JsonElement serialize(A src, Type typeOfSrc,
          JsonSerializationContext context) {
        return new JsonPrimitive("registeredSerializer");
      }
    };
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(A.class, serializer)
      .create();
    String json = gson.toJson(new A("abcd"));
    assertThat(json).isEqualTo("\"registeredSerializer\"");
    A target = gson.fromJson("abcd", A.class);
    assertThat(target.value).isEqualTo("jsonAdapter");
  }

  /**
   * The deserializer overrides Json adapter, but for serializer the jsonAdapter is used.
   */
  @Test
  public void testRegisteredDeserializerOverridesJsonAdapter() {
    JsonDeserializer<A> deserializer = new JsonDeserializer<A>() {
      @Override public A deserialize(JsonElement json, Type typeOfT,
          JsonDeserializationContext context) throws JsonParseException {
        return new A("registeredDeserializer");
      }
    };
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(A.class, deserializer)
      .create();
    String json = gson.toJson(new A("abcd"));
    assertThat(json).isEqualTo("\"jsonAdapter\"");
    A target = gson.fromJson("abcd", A.class);
    assertThat(target.value).isEqualTo("registeredDeserializer");
  }

  @Test
  public void testIncorrectTypeAdapterFails() {
    try {
      String json = new Gson().toJson(new ClassWithIncorrectJsonAdapter("bar"));
      fail(json);
    } catch (ClassCastException expected) {}
  }

  @Test
  public void testSuperclassTypeAdapterNotInvoked() {
    String json = new Gson().toJson(new B("bar"));
    assertThat(json).doesNotContain("jsonAdapter");
  }

  @Test
  public void testNullSafeObjectFromJson() {
    Gson gson = new Gson();
    NullableClass fromJson = gson.fromJson("null", NullableClass.class);
    assertThat(fromJson).isNull();
  }

  @JsonAdapter(A.JsonAdapter.class)
  private static class A {
    final String value;
    A(String value) {
      this.value = value;
    }
    static final class JsonAdapter extends TypeAdapter<A> {
      @Override public void write(JsonWriter out, A value) throws IOException {
        out.value("jsonAdapter");
      }
      @Override public A read(JsonReader in) throws IOException {
        in.nextString();
        return new A("jsonAdapter");
      }
    }
  }

  @JsonAdapter(C.JsonAdapterFactory.class)
  private static class C {
    final String value;
    C(String value) {
      this.value = value;
    }
    static final class JsonAdapterFactory implements TypeAdapterFactory {
      @Override public <T> TypeAdapter<T> create(Gson gson, final TypeToken<T> type) {
        return new TypeAdapter<T>() {
          @Override public void write(JsonWriter out, T value) throws IOException {
            out.value("jsonAdapterFactory");
          }
          @SuppressWarnings("unchecked")
          @Override public T read(JsonReader in) throws IOException {
            in.nextString();
            return (T) new C("jsonAdapterFactory");
          }
        };
      }
    }
  }

  private static final class B extends A {
    B(String value) {
      super(value);
    }
  }
  // Note that the type is NOT TypeAdapter<ClassWithIncorrectJsonAdapter> so this
  // should cause error
  @JsonAdapter(A.JsonAdapter.class)
  private static final class ClassWithIncorrectJsonAdapter {
    @SuppressWarnings("unused") final String value;
    ClassWithIncorrectJsonAdapter(String value) {
      this.value = value;
    }
  }

  // This class is used in JsonAdapter Javadoc as an example
  @JsonAdapter(UserJsonAdapter.class)
  private static class User {
    final String firstName, lastName;
    User(String firstName, String lastName) {
      this.firstName = firstName;
      this.lastName = lastName;
    }
  }
  private static class UserJsonAdapter extends TypeAdapter<User> {
    @Override public void write(JsonWriter out, User user) throws IOException {
      // implement write: combine firstName and lastName into name
      out.beginObject();
      out.name("name");
      out.value(user.firstName + " " + user.lastName);
      out.endObject();
      // implement the write method
    }
    @Override public User read(JsonReader in) throws IOException {
      // implement read: split name into firstName and lastName
      in.beginObject();
      in.nextName();
      List<String> nameParts = Splitter.on(" ").splitToList(in.nextString());
      in.endObject();
      return new User(nameParts.get(0), nameParts.get(1));
    }
  }

  @JsonAdapter(value = NullableClassJsonAdapter.class)
  private static class NullableClass {
  }

  private static class NullableClassJsonAdapter extends TypeAdapter<NullableClass> {
    @Override
    public void write(JsonWriter out, NullableClass value) throws IOException {
      out.value("nullable");
    }

    @Override
    public NullableClass read(JsonReader in) throws IOException {
      in.nextString();
      return new NullableClass();
    }
  }

  @JsonAdapter(FooJsonAdapter.class)
  private static enum Foo { BAR, BAZ }
  private static class FooJsonAdapter extends TypeAdapter<Foo> {
    @Override public void write(JsonWriter out, Foo value) throws IOException {
      out.value(value.name().toLowerCase(Locale.US));
    }

    @Override public Foo read(JsonReader in) throws IOException {
      return Foo.valueOf(in.nextString().toUpperCase(Locale.US));
    }
  }

  @Test
  public void testIncorrectJsonAdapterType() {
    try {
      new Gson().toJson(new D());
      fail();
    } catch (IllegalArgumentException expected) {}
  }
  @JsonAdapter(Integer.class)
  private static final class D {
    @SuppressWarnings("unused") final String value = "a";
  }

  /**
   * Verifies that {@link TypeAdapterFactory} specified by {@code @JsonAdapter} can
   * call {@link Gson#getDelegateAdapter} without any issues, despite the factory
   * not being directly registered on Gson.
   */
  @Test
  public void testDelegatingAdapterFactory() {
    @SuppressWarnings("unchecked")
    WithDelegatingFactory<String> deserialized = new Gson().fromJson("{\"custom\":{\"f\":\"de\"}}", WithDelegatingFactory.class);
    assertThat(deserialized.f).isEqualTo("de");

    deserialized = new Gson().fromJson("{\"custom\":{\"f\":\"de\"}}", new TypeToken<WithDelegatingFactory<String>>() {});
    assertThat(deserialized.f).isEqualTo("de");

    WithDelegatingFactory<String> serialized = new WithDelegatingFactory<>("se");
    assertThat(new Gson().toJson(serialized)).isEqualTo("{\"custom\":{\"f\":\"se\"}}");
  }
  @JsonAdapter(WithDelegatingFactory.Factory.class)
  private static class WithDelegatingFactory<T> {
    T f;

    WithDelegatingFactory(T f) {
      this.f = f;
    }

    static class Factory implements TypeAdapterFactory {
      @Override
      public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (type.getRawType() != WithDelegatingFactory.class) {
          return null;
        }

        TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);

        return new TypeAdapter<T>() {
          @Override
          public T read(JsonReader in) throws IOException {
            // Perform custom deserialization
            in.beginObject();
            assertThat(in.nextName()).isEqualTo("custom");
            T t = delegate.read(in);
            in.endObject();

            return t;
          }

          @Override
          public void write(JsonWriter out, T value) throws IOException {
            // Perform custom serialization
            out.beginObject();
            out.name("custom");
            delegate.write(out, value);
            out.endObject();
          }
        };
      }
    }
  }

  /**
   * Similar to {@link #testDelegatingAdapterFactory}, except that the delegate is not
   * looked up in {@code create} but instead in the adapter methods.
   */
  @Test
  public void testDelegatingAdapterFactory_Delayed() {
    WithDelayedDelegatingFactory deserialized = new Gson().fromJson("{\"custom\":{\"f\":\"de\"}}", WithDelayedDelegatingFactory.class);
    assertThat(deserialized.f).isEqualTo("de");

    WithDelayedDelegatingFactory serialized = new WithDelayedDelegatingFactory("se");
    assertThat(new Gson().toJson(serialized)).isEqualTo("{\"custom\":{\"f\":\"se\"}}");
  }
  @JsonAdapter(WithDelayedDelegatingFactory.Factory.class)
  private static class WithDelayedDelegatingFactory {
    String f;

    WithDelayedDelegatingFactory(String f) {
      this.f = f;
    }

    static class Factory implements TypeAdapterFactory {
      @Override
      public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        return new TypeAdapter<T>() {
          @SuppressWarnings("SameNameButDifferent") // suppress Error Prone warning; should be clear that `Factory` refers to enclosing class
          private TypeAdapter<T> delegate() {
            return gson.getDelegateAdapter(Factory.this, type);
          }

          @Override
          public T read(JsonReader in) throws IOException {
            // Perform custom deserialization
            in.beginObject();
            assertThat(in.nextName()).isEqualTo("custom");
            T t = delegate().read(in);
            in.endObject();

            return t;
          }

          @Override
          public void write(JsonWriter out, T value) throws IOException {
            // Perform custom serialization
            out.beginObject();
            out.name("custom");
            delegate().write(out, value);
            out.endObject();
          }
        };
      }
    }
  }

  /**
   * Tests behavior of {@link Gson#getDelegateAdapter} when <i>different</i> instances of the same
   * factory class are used; one registered on the {@code GsonBuilder} and the other implicitly
   * through {@code @JsonAdapter}.
   */
  @Test
  public void testDelegating_SameFactoryClass() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(new WithDelegatingFactory.Factory())
        .create();

    // Should use both factories, and therefore have `{"custom": ... }` twice
    WithDelegatingFactory<?> deserialized = gson.fromJson("{\"custom\":{\"custom\":{\"f\":\"de\"}}}", WithDelegatingFactory.class);
    assertThat(deserialized.f).isEqualTo("de");

    WithDelegatingFactory<String> serialized = new WithDelegatingFactory<>("se");
    assertThat(gson.toJson(serialized)).isEqualTo("{\"custom\":{\"custom\":{\"f\":\"se\"}}}");
  }

  /**
   * Tests behavior of {@link Gson#getDelegateAdapter} when the <i>same</i> instance of a factory
   * is used (through {@link InstanceCreator}).
   *
   * <p><b>Important:</b> This situation is likely a rare corner case; the purpose of this test is
   * to verify that Gson behaves reasonable, mainly that it does not cause a {@link StackOverflowError}
   * due to infinite recursion. This test is not intended to dictate an expected behavior.
   */
  @Test
  public void testDelegating_SameFactoryInstance() {
    WithDelegatingFactory.Factory factory = new WithDelegatingFactory.Factory();

    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(factory)
        // Always provides same instance for factory
        .registerTypeAdapter(WithDelegatingFactory.Factory.class, (InstanceCreator<?>) type -> factory)
        .create();

    // Current Gson.getDelegateAdapter implementation cannot tell when call is related to @JsonAdapter
    // or not, it can only work based on the `skipPast` factory, so if the same factory instance is used
    // the one registered with `GsonBuilder.registerTypeAdapterFactory` actually skips past the @JsonAdapter
    // one, so the JSON string is `{"custom": ...}` instead of `{"custom":{"custom":...}}`
    WithDelegatingFactory<?> deserialized = gson.fromJson("{\"custom\":{\"f\":\"de\"}}", WithDelegatingFactory.class);
    assertThat(deserialized.f).isEqualTo("de");

    WithDelegatingFactory<String> serialized = new WithDelegatingFactory<>("se");
    assertThat(gson.toJson(serialized)).isEqualTo("{\"custom\":{\"f\":\"se\"}}");
  }

  /**
   * Tests behavior of {@link Gson#getDelegateAdapter} when <i>different</i> instances of the same
   * factory class are used; one specified with {@code @JsonAdapter} on a class, and the other specified
   * with {@code @JsonAdapter} on a field of that class.
   *
   * <p><b>Important:</b> This situation is likely a rare corner case; the purpose of this test is
   * to verify that Gson behaves reasonable, mainly that it does not cause a {@link StackOverflowError}
   * due to infinite recursion. This test is not intended to dictate an expected behavior.
   */
  @Test
  public void testDelegating_SameFactoryClass_OnClassAndField() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(String.class, new TypeAdapter<String>() {
          @Override
          public String read(JsonReader in) throws IOException {
            return in.nextString() + "-str";
          }

          @Override
          public void write(JsonWriter out, String value) throws IOException {
            out.value(value + "-str");
          }
        })
        .create();

    // Should use both factories, and therefore have `{"custom": ... }` once for class and once for the field,
    // and for field also properly delegate to custom String adapter defined above
    WithDelegatingFactoryOnClassAndField deserialized = gson.fromJson("{\"custom\":{\"f\":{\"custom\":\"de\"}}}",
        WithDelegatingFactoryOnClassAndField.class);
    assertThat(deserialized.f).isEqualTo("de-str");

    WithDelegatingFactoryOnClassAndField serialized = new WithDelegatingFactoryOnClassAndField("se");
    assertThat(gson.toJson(serialized)).isEqualTo("{\"custom\":{\"f\":{\"custom\":\"se-str\"}}}");
  }

  /**
   * Tests behavior of {@link Gson#getDelegateAdapter} when the <i>same</i> instance of a factory
   * is used (through {@link InstanceCreator}); specified with {@code @JsonAdapter} on a class,
   * and also specified with {@code @JsonAdapter} on a field of that class.
   *
   * <p><b>Important:</b> This situation is likely a rare corner case; the purpose of this test is
   * to verify that Gson behaves reasonable, mainly that it does not cause a {@link StackOverflowError}
   * due to infinite recursion. This test is not intended to dictate an expected behavior.
   */
  @Test
  public void testDelegating_SameFactoryInstance_OnClassAndField() {
    WithDelegatingFactoryOnClassAndField.Factory factory = new WithDelegatingFactoryOnClassAndField.Factory();

    Gson gson = new GsonBuilder()
        .registerTypeAdapter(String.class, new TypeAdapter<String>() {
          @Override
          public String read(JsonReader in) throws IOException {
            return in.nextString() + "-str";
          }

          @Override
          public void write(JsonWriter out, String value) throws IOException {
            out.value(value + "-str");
          }
        })
        // Always provides same instance for factory
        .registerTypeAdapter(WithDelegatingFactoryOnClassAndField.Factory.class, (InstanceCreator<?>) type -> factory)
        .create();

    // Because field type (`String`) differs from declaring class, JsonAdapterAnnotationTypeAdapterFactory does
    // not confuse factories and this behaves as expected: Both the declaring class and the field each have
    // `{"custom": ...}` and delegation for the field to the custom String adapter defined above works properly
    WithDelegatingFactoryOnClassAndField deserialized = gson.fromJson("{\"custom\":{\"f\":{\"custom\":\"de\"}}}",
        WithDelegatingFactoryOnClassAndField.class);
    assertThat(deserialized.f).isEqualTo("de-str");

    WithDelegatingFactoryOnClassAndField serialized = new WithDelegatingFactoryOnClassAndField("se");
    assertThat(gson.toJson(serialized)).isEqualTo("{\"custom\":{\"f\":{\"custom\":\"se-str\"}}}");
  }
  // Same factory class specified on class and one of its fields
  @JsonAdapter(WithDelegatingFactoryOnClassAndField.Factory.class)
  private static class WithDelegatingFactoryOnClassAndField {
    @SuppressWarnings("SameNameButDifferent") // suppress Error Prone warning; should be clear that `Factory` refers to nested class
    @JsonAdapter(Factory.class)
    String f;

    WithDelegatingFactoryOnClassAndField(String f) {
      this.f = f;
    }

    static class Factory implements TypeAdapterFactory {
      @Override
      public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);

        return new TypeAdapter<T>() {
          @Override
          public T read(JsonReader in) throws IOException {
            // Perform custom deserialization
            in.beginObject();
            assertThat(in.nextName()).isEqualTo("custom");
            T t = delegate.read(in);
            in.endObject();

            return t;
          }

          @Override
          public void write(JsonWriter out, T value) throws IOException {
            // Perform custom serialization
            out.beginObject();
            out.name("custom");
            delegate.write(out, value);
            out.endObject();
          }
        };
      }
    }
  }

  /**
   * Tests usage of {@link JsonSerializer} as {@link JsonAdapter} value
   */
  @Test
  public void testJsonSerializer() {
    Gson gson = new Gson();
    // Verify that delegate deserializer (reflection deserializer) is used
    WithJsonSerializer deserialized = gson.fromJson("{\"f\":\"test\"}", WithJsonSerializer.class);
    assertThat(deserialized.f).isEqualTo("test");

    String json = gson.toJson(new WithJsonSerializer());
    // Uses custom serializer which always returns `true`
    assertThat(json).isEqualTo("true");
  }
  @JsonAdapter(WithJsonSerializer.Serializer.class)
  private static class WithJsonSerializer {
    String f = "";

    static class Serializer implements JsonSerializer<WithJsonSerializer> {
      @Override
      public JsonElement serialize(WithJsonSerializer src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(true);
      }
    }
  }

  /**
   * Tests usage of {@link JsonDeserializer} as {@link JsonAdapter} value
   */
  @Test
  public void testJsonDeserializer() {
    Gson gson = new Gson();
    WithJsonDeserializer deserialized = gson.fromJson("{\"f\":\"test\"}", WithJsonDeserializer.class);
    // Uses custom deserializer which always uses "123" as field value
    assertThat(deserialized.f).isEqualTo("123");

    // Verify that delegate serializer (reflection serializer) is used
    String json = gson.toJson(new WithJsonDeserializer("abc"));
    assertThat(json).isEqualTo("{\"f\":\"abc\"}");
  }
  @JsonAdapter(WithJsonDeserializer.Deserializer.class)
  private static class WithJsonDeserializer {
    String f;

    WithJsonDeserializer(String f) {
      this.f = f;
    }

    static class Deserializer implements JsonDeserializer<WithJsonDeserializer> {
      @Override
      public WithJsonDeserializer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return new WithJsonDeserializer("123");
      }
    }
  }
}
