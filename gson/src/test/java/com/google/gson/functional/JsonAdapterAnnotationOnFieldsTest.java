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

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

/** Functional tests for the {@link JsonAdapter} annotation on fields. */
public final class JsonAdapterAnnotationOnFieldsTest {
  @Test
  public void testClassAnnotationAdapterTakesPrecedenceOverDefault() {
    Gson gson = new Gson();
    String json = gson.toJson(new Computer(new User("Inderjeet Singh")));
    assertThat(json).isEqualTo("{\"user\":\"UserClassAnnotationAdapter\"}");
    Computer computer = gson.fromJson("{'user':'Inderjeet Singh'}", Computer.class);
    assertThat(computer.user.name).isEqualTo("UserClassAnnotationAdapter");
  }

  @Test
  public void testClassAnnotationAdapterFactoryTakesPrecedenceOverDefault() {
    Gson gson = new Gson();
    String json = gson.toJson(new Gizmo(new Part("Part")));
    assertThat(json).isEqualTo("{\"part\":\"GizmoPartTypeAdapterFactory\"}");
    Gizmo computer = gson.fromJson("{'part':'Part'}", Gizmo.class);
    assertThat(computer.part.name).isEqualTo("GizmoPartTypeAdapterFactory");
  }

  @Test
  public void testRegisteredTypeAdapterTakesPrecedenceOverClassAnnotationAdapter() {
    Gson gson =
        new GsonBuilder().registerTypeAdapter(User.class, new RegisteredUserAdapter()).create();
    String json = gson.toJson(new Computer(new User("Inderjeet Singh")));
    assertThat(json).isEqualTo("{\"user\":\"RegisteredUserAdapter\"}");
    Computer computer = gson.fromJson("{'user':'Inderjeet Singh'}", Computer.class);
    assertThat(computer.user.name).isEqualTo("RegisteredUserAdapter");
  }

  @Test
  public void testFieldAnnotationTakesPrecedenceOverRegisteredTypeAdapter() {
    Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(
                Part.class,
                new TypeAdapter<Part>() {
                  @Override
                  public void write(JsonWriter out, Part part) {
                    throw new AssertionError();
                  }

                  @Override
                  public Part read(JsonReader in) {
                    throw new AssertionError();
                  }
                })
            .create();
    String json = gson.toJson(new Gadget(new Part("screen")));
    assertThat(json).isEqualTo("{\"part\":\"PartJsonFieldAnnotationAdapter\"}");
    Gadget gadget = gson.fromJson("{'part':'screen'}", Gadget.class);
    assertThat(gadget.part.name).isEqualTo("PartJsonFieldAnnotationAdapter");
  }

  @Test
  public void testFieldAnnotationTakesPrecedenceOverClassAnnotation() {
    Gson gson = new Gson();
    String json = gson.toJson(new Computer2(new User("Inderjeet Singh")));
    assertThat(json).isEqualTo("{\"user\":\"UserFieldAnnotationAdapter\"}");
    Computer2 target = gson.fromJson("{'user':'Interjeet Singh'}", Computer2.class);
    assertThat(target.user.name).isEqualTo("UserFieldAnnotationAdapter");
  }

  private static final class Gadget {
    @JsonAdapter(PartJsonFieldAnnotationAdapter.class)
    final Part part;

    Gadget(Part part) {
      this.part = part;
    }
  }

  private static final class Gizmo {
    @JsonAdapter(GizmoPartTypeAdapterFactory.class)
    final Part part;

    Gizmo(Part part) {
      this.part = part;
    }
  }

  private static final class Part {
    final String name;

    public Part(String name) {
      this.name = name;
    }
  }

  private static class PartJsonFieldAnnotationAdapter extends TypeAdapter<Part> {
    @Override
    public void write(JsonWriter out, Part part) throws IOException {
      out.value("PartJsonFieldAnnotationAdapter");
    }

    @Override
    public Part read(JsonReader in) throws IOException {
      String unused = in.nextString();
      return new Part("PartJsonFieldAnnotationAdapter");
    }
  }

  private static class GizmoPartTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      return new TypeAdapter<>() {
        @Override
        public void write(JsonWriter out, T value) throws IOException {
          out.value("GizmoPartTypeAdapterFactory");
        }

        @SuppressWarnings("unchecked")
        @Override
        public T read(JsonReader in) throws IOException {
          String unused = in.nextString();
          return (T) new Part("GizmoPartTypeAdapterFactory");
        }
      };
    }
  }

  private static final class Computer {
    final User user;

    Computer(User user) {
      this.user = user;
    }
  }

  @JsonAdapter(UserClassAnnotationAdapter.class)
  private static class User {
    public final String name;

    private User(String name) {
      this.name = name;
    }
  }

  private static class UserClassAnnotationAdapter extends TypeAdapter<User> {
    @Override
    public void write(JsonWriter out, User user) throws IOException {
      out.value("UserClassAnnotationAdapter");
    }

    @Override
    public User read(JsonReader in) throws IOException {
      String unused = in.nextString();
      return new User("UserClassAnnotationAdapter");
    }
  }

  private static final class Computer2 {
    // overrides the JsonAdapter annotation of User with this
    @JsonAdapter(UserFieldAnnotationAdapter.class)
    final User user;

    Computer2(User user) {
      this.user = user;
    }
  }

  private static final class UserFieldAnnotationAdapter extends TypeAdapter<User> {
    @Override
    public void write(JsonWriter out, User user) throws IOException {
      out.value("UserFieldAnnotationAdapter");
    }

    @Override
    public User read(JsonReader in) throws IOException {
      String unused = in.nextString();
      return new User("UserFieldAnnotationAdapter");
    }
  }

  private static final class RegisteredUserAdapter extends TypeAdapter<User> {
    @Override
    public void write(JsonWriter out, User user) throws IOException {
      out.value("RegisteredUserAdapter");
    }

    @Override
    public User read(JsonReader in) throws IOException {
      String unused = in.nextString();
      return new User("RegisteredUserAdapter");
    }
  }

  @Test
  public void testJsonAdapterInvokedOnlyForAnnotatedFields() {
    Gson gson = new Gson();
    String json = "{'part1':'name','part2':{'name':'name2'}}";
    GadgetWithTwoParts gadget = gson.fromJson(json, GadgetWithTwoParts.class);
    assertThat(gadget.part1.name).isEqualTo("PartJsonFieldAnnotationAdapter");
    assertThat(gadget.part2.name).isEqualTo("name2");
  }

  private static final class GadgetWithTwoParts {
    @JsonAdapter(PartJsonFieldAnnotationAdapter.class)
    final Part part1;

    final Part part2; // Doesn't have the JsonAdapter annotation

    @SuppressWarnings("unused")
    GadgetWithTwoParts(Part part1, Part part2) {
      this.part1 = part1;
      this.part2 = part2;
    }
  }

  @Test
  public void testJsonAdapterWrappedInNullSafeAsRequested() {
    Gson gson = new Gson();
    String fromJson = "{'part':null}";

    GadgetWithOptionalPart gadget = gson.fromJson(fromJson, GadgetWithOptionalPart.class);
    assertThat(gadget.part).isNull();

    String toJson = gson.toJson(gadget);
    assertThat(toJson).doesNotContain("PartJsonFieldAnnotationAdapter");
  }

  private static final class GadgetWithOptionalPart {
    @JsonAdapter(value = PartJsonFieldAnnotationAdapter.class)
    final Part part;

    private GadgetWithOptionalPart(Part part) {
      this.part = part;
    }
  }

  /** Regression test contributed through https://github.com/google/gson/issues/831 */
  @Test
  public void testNonPrimitiveFieldAnnotationTakesPrecedenceOverDefault() {
    Gson gson = new Gson();
    String json = gson.toJson(new GadgetWithOptionalPart(new Part("foo")));
    assertThat(json).isEqualTo("{\"part\":\"PartJsonFieldAnnotationAdapter\"}");
    GadgetWithOptionalPart gadget = gson.fromJson("{'part':'foo'}", GadgetWithOptionalPart.class);
    assertThat(gadget.part.name).isEqualTo("PartJsonFieldAnnotationAdapter");
  }

  /** Regression test contributed through https://github.com/google/gson/issues/831 */
  @Test
  public void testPrimitiveFieldAnnotationTakesPrecedenceOverDefault() {
    Gson gson = new Gson();
    String json = gson.toJson(new GadgetWithPrimitivePart(42));
    assertThat(json).isEqualTo("{\"part\":\"42\"}");
    GadgetWithPrimitivePart gadget = gson.fromJson(json, GadgetWithPrimitivePart.class);
    assertThat(gadget.part).isEqualTo(42);
  }

  private static final class GadgetWithPrimitivePart {
    @JsonAdapter(LongToStringTypeAdapterFactory.class)
    final long part;

    private GadgetWithPrimitivePart(long part) {
      this.part = part;
    }
  }

  private static final class LongToStringTypeAdapterFactory implements TypeAdapterFactory {
    static final TypeAdapter<Long> ADAPTER =
        new TypeAdapter<>() {
          @Override
          public void write(JsonWriter out, Long value) throws IOException {
            out.value(value.toString());
          }

          @Override
          public Long read(JsonReader in) throws IOException {
            return in.nextLong();
          }
        };

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      Class<?> cls = type.getRawType();
      if (Long.class.isAssignableFrom(cls)) {
        return (TypeAdapter<T>) ADAPTER;
      } else if (long.class.isAssignableFrom(cls)) {
        return (TypeAdapter<T>) ADAPTER;
      }
      throw new IllegalStateException(
          "Non-long field of type "
              + type
              + " annotated with @JsonAdapter(LongToStringTypeAdapterFactory.class)");
    }
  }

  @Test
  public void testFieldAnnotationWorksForParameterizedType() {
    Gson gson = new Gson();
    String json = gson.toJson(new Gizmo2(Arrays.asList(new Part("Part"))));
    assertThat(json).isEqualTo("{\"part\":\"GizmoPartTypeAdapterFactory\"}");
    Gizmo2 computer = gson.fromJson("{'part':'Part'}", Gizmo2.class);
    assertThat(computer.part.get(0).name).isEqualTo("GizmoPartTypeAdapterFactory");
  }

  private static final class Gizmo2 {
    @JsonAdapter(Gizmo2PartTypeAdapterFactory.class)
    List<Part> part;

    Gizmo2(List<Part> part) {
      this.part = part;
    }
  }

  private static class Gizmo2PartTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      return new TypeAdapter<>() {
        @Override
        public void write(JsonWriter out, T value) throws IOException {
          out.value("GizmoPartTypeAdapterFactory");
        }

        @SuppressWarnings("unchecked")
        @Override
        public T read(JsonReader in) throws IOException {
          String unused = in.nextString();
          return (T) Arrays.asList(new Part("GizmoPartTypeAdapterFactory"));
        }
      };
    }
  }

  /**
   * Verify that {@link JsonAdapter} annotation can overwrite adapters which can normally not be
   * overwritten (in this case adapter for {@link JsonElement}).
   */
  @Test
  public void testOverwriteBuiltIn() {
    BuiltInOverwriting obj = new BuiltInOverwriting();
    obj.f = new JsonPrimitive(true);
    String json = new Gson().toJson(obj);
    assertThat(json).isEqualTo("{\"f\":\"" + JsonElementAdapter.SERIALIZED + "\"}");

    BuiltInOverwriting deserialized = new Gson().fromJson("{\"f\": 2}", BuiltInOverwriting.class);
    assertThat(deserialized.f).isEqualTo(JsonElementAdapter.DESERIALIZED);
  }

  private static class BuiltInOverwriting {
    @JsonAdapter(JsonElementAdapter.class)
    JsonElement f;
  }

  private static class JsonElementAdapter extends TypeAdapter<JsonElement> {
    static final JsonPrimitive DESERIALIZED = new JsonPrimitive("deserialized hardcoded");

    @Override
    public JsonElement read(JsonReader in) throws IOException {
      in.skipValue();
      return DESERIALIZED;
    }

    static final String SERIALIZED = "serialized hardcoded";

    @Override
    public void write(JsonWriter out, JsonElement value) throws IOException {
      out.value(SERIALIZED);
    }
  }

  /**
   * Verify that exclusion strategy preventing serialization has higher precedence than {@link
   * JsonAdapter} annotation.
   */
  @Test
  public void testExcludeSerializePrecedence() {
    Gson gson =
        new GsonBuilder()
            .addSerializationExclusionStrategy(
                new ExclusionStrategy() {
                  @Override
                  public boolean shouldSkipField(FieldAttributes f) {
                    return true;
                  }

                  @Override
                  public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                  }
                })
            .create();

    DelegatingAndOverwriting obj = new DelegatingAndOverwriting();
    obj.f = 1;
    obj.f2 = new JsonPrimitive(2);
    obj.f3 = new JsonPrimitive(true);
    String json = gson.toJson(obj);
    assertThat(json).isEqualTo("{}");

    DelegatingAndOverwriting deserialized =
        gson.fromJson("{\"f\":1,\"f2\":2,\"f3\":3}", DelegatingAndOverwriting.class);
    assertThat(deserialized.f).isEqualTo(Integer.valueOf(1));
    assertThat(deserialized.f2).isEqualTo(new JsonPrimitive(2));
    // Verify that for deserialization type adapter specified by @JsonAdapter is used
    assertThat(deserialized.f3).isEqualTo(JsonElementAdapter.DESERIALIZED);
  }

  /**
   * Verify that exclusion strategy preventing deserialization has higher precedence than {@link
   * JsonAdapter} annotation.
   */
  @Test
  public void testExcludeDeserializePrecedence() {
    Gson gson =
        new GsonBuilder()
            .addDeserializationExclusionStrategy(
                new ExclusionStrategy() {
                  @Override
                  public boolean shouldSkipField(FieldAttributes f) {
                    return true;
                  }

                  @Override
                  public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                  }
                })
            .create();

    DelegatingAndOverwriting obj = new DelegatingAndOverwriting();
    obj.f = 1;
    obj.f2 = new JsonPrimitive(2);
    obj.f3 = new JsonPrimitive(true);
    String json = gson.toJson(obj);
    // Verify that for serialization type adapters specified by @JsonAdapter are used
    assertThat(json)
        .isEqualTo("{\"f\":1,\"f2\":2,\"f3\":\"" + JsonElementAdapter.SERIALIZED + "\"}");

    DelegatingAndOverwriting deserialized =
        gson.fromJson("{\"f\":1,\"f2\":2,\"f3\":3}", DelegatingAndOverwriting.class);
    assertThat(deserialized.f).isNull();
    assertThat(deserialized.f2).isNull();
    assertThat(deserialized.f3).isNull();
  }

  /**
   * Verify that exclusion strategy preventing serialization and deserialization has higher
   * precedence than {@link JsonAdapter} annotation.
   *
   * <p>This is a separate test method because {@link ReflectiveTypeAdapterFactory} handles this
   * case differently.
   */
  @Test
  public void testExcludePrecedence() {
    Gson gson =
        new GsonBuilder()
            .setExclusionStrategies(
                new ExclusionStrategy() {
                  @Override
                  public boolean shouldSkipField(FieldAttributes f) {
                    return true;
                  }

                  @Override
                  public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                  }
                })
            .create();

    DelegatingAndOverwriting obj = new DelegatingAndOverwriting();
    obj.f = 1;
    obj.f2 = new JsonPrimitive(2);
    obj.f3 = new JsonPrimitive(true);
    String json = gson.toJson(obj);
    assertThat(json).isEqualTo("{}");

    DelegatingAndOverwriting deserialized =
        gson.fromJson("{\"f\":1,\"f2\":2,\"f3\":3}", DelegatingAndOverwriting.class);
    assertThat(deserialized.f).isNull();
    assertThat(deserialized.f2).isNull();
    assertThat(deserialized.f3).isNull();
  }

  private static class DelegatingAndOverwriting {
    @JsonAdapter(DelegatingAdapterFactory.class)
    Integer f;

    @JsonAdapter(DelegatingAdapterFactory.class)
    JsonElement f2;

    // Also have non-delegating adapter to make tests handle both cases
    @JsonAdapter(JsonElementAdapter.class)
    JsonElement f3;

    static class DelegatingAdapterFactory implements TypeAdapterFactory {
      @Override
      public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        return gson.getDelegateAdapter(this, type);
      }
    }
  }

  /**
   * Verifies that {@link TypeAdapterFactory} specified by {@code @JsonAdapter} can call {@link
   * Gson#getDelegateAdapter} without any issues, despite the factory not being directly registered
   * on Gson.
   */
  @Test
  public void testDelegatingAdapterFactory() {
    @SuppressWarnings("unchecked")
    WithDelegatingFactory<String> deserialized =
        new Gson().fromJson("{\"f\":\"test\"}", WithDelegatingFactory.class);
    assertThat(deserialized.f).isEqualTo("test-custom");

    deserialized =
        new Gson().fromJson("{\"f\":\"test\"}", new TypeToken<WithDelegatingFactory<String>>() {});
    assertThat(deserialized.f).isEqualTo("test-custom");

    WithDelegatingFactory<String> serialized = new WithDelegatingFactory<>();
    serialized.f = "value";
    assertThat(new Gson().toJson(serialized)).isEqualTo("{\"f\":\"value-custom\"}");
  }

  private static class WithDelegatingFactory<T> {
    // suppress Error Prone warning; should be clear that `Factory` refers to nested class
    @SuppressWarnings("SameNameButDifferent")
    @JsonAdapter(Factory.class)
    T f;

    static class Factory implements TypeAdapterFactory {
      @SuppressWarnings("unchecked")
      @Override
      public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        TypeAdapter<String> delegate = (TypeAdapter<String>) gson.getDelegateAdapter(this, type);

        return (TypeAdapter<T>)
            new TypeAdapter<String>() {
              @Override
              public String read(JsonReader in) throws IOException {
                // Perform custom deserialization
                return delegate.read(in) + "-custom";
              }

              @Override
              public void write(JsonWriter out, String value) throws IOException {
                // Perform custom serialization
                delegate.write(out, value + "-custom");
              }
            };
      }
    }
  }

  /**
   * Similar to {@link #testDelegatingAdapterFactory}, except that the delegate is not looked up in
   * {@code create} but instead in the adapter methods.
   */
  @Test
  public void testDelegatingAdapterFactory_Delayed() {
    WithDelayedDelegatingFactory deserialized =
        new Gson().fromJson("{\"f\":\"test\"}", WithDelayedDelegatingFactory.class);
    assertThat(deserialized.f).isEqualTo("test-custom");

    WithDelayedDelegatingFactory serialized = new WithDelayedDelegatingFactory();
    serialized.f = "value";
    assertThat(new Gson().toJson(serialized)).isEqualTo("{\"f\":\"value-custom\"}");
  }

  // suppress Error Prone warning; should be clear that `Factory` refers to nested class
  @SuppressWarnings("SameNameButDifferent")
  private static class WithDelayedDelegatingFactory {
    @JsonAdapter(Factory.class)
    String f;

    static class Factory implements TypeAdapterFactory {
      @SuppressWarnings("unchecked")
      @Override
      public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        return (TypeAdapter<T>)
            new TypeAdapter<String>() {
              private TypeAdapter<String> delegate() {
                return (TypeAdapter<String>) gson.getDelegateAdapter(Factory.this, type);
              }

              @Override
              public String read(JsonReader in) throws IOException {
                // Perform custom deserialization
                return delegate().read(in) + "-custom";
              }

              @Override
              public void write(JsonWriter out, String value) throws IOException {
                // Perform custom serialization
                delegate().write(out, value + "-custom");
              }
            };
      }
    }
  }

  /**
   * Tests usage of {@link Gson#getAdapter(TypeToken)} in the {@code create} method of the factory.
   * Existing code was using that as workaround because {@link Gson#getDelegateAdapter} previously
   * did not work in combination with {@code @JsonAdapter}, see
   * https://github.com/google/gson/issues/1028.
   */
  @Test
  public void testGetAdapterDelegation() {
    Gson gson = new Gson();
    GetAdapterDelegation deserialized = gson.fromJson("{\"f\":\"de\"}", GetAdapterDelegation.class);
    assertThat(deserialized.f).isEqualTo("de-custom");

    String json = gson.toJson(new GetAdapterDelegation("se"));
    assertThat(json).isEqualTo("{\"f\":\"se-custom\"}");
  }

  private static class GetAdapterDelegation {
    // suppress Error Prone warning; should be clear that `Factory` refers to nested class
    @SuppressWarnings("SameNameButDifferent")
    @JsonAdapter(Factory.class)
    String f;

    GetAdapterDelegation(String f) {
      this.f = f;
    }

    static class Factory implements TypeAdapterFactory {
      @SuppressWarnings("unchecked")
      @Override
      public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        // Uses `Gson.getAdapter` instead of `Gson.getDelegateAdapter`
        TypeAdapter<String> delegate = (TypeAdapter<String>) gson.getAdapter(type);

        return (TypeAdapter<T>)
            new TypeAdapter<String>() {
              @Override
              public String read(JsonReader in) throws IOException {
                return delegate.read(in) + "-custom";
              }

              @Override
              public void write(JsonWriter out, String value) throws IOException {
                delegate.write(out, value + "-custom");
              }
            };
      }
    }
  }

  /** Tests usage of {@link JsonSerializer} as {@link JsonAdapter} value on a field */
  @Test
  public void testJsonSerializer() {
    Gson gson = new Gson();
    // Verify that delegate deserializer for List is used
    WithJsonSerializer deserialized = gson.fromJson("{\"f\":[1,2,3]}", WithJsonSerializer.class);
    assertThat(deserialized.f).isEqualTo(Arrays.asList(1, 2, 3));

    String json = gson.toJson(new WithJsonSerializer());
    // Uses custom serializer which always returns `true`
    assertThat(json).isEqualTo("{\"f\":true}");
  }

  private static class WithJsonSerializer {
    @JsonAdapter(Serializer.class)
    List<Integer> f = Collections.emptyList();

    static class Serializer implements JsonSerializer<List<Integer>> {
      @Override
      public JsonElement serialize(
          List<Integer> src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(true);
      }
    }
  }

  /** Tests usage of {@link JsonDeserializer} as {@link JsonAdapter} value on a field */
  @Test
  public void testJsonDeserializer() {
    Gson gson = new Gson();
    WithJsonDeserializer deserialized = gson.fromJson("{\"f\":[5]}", WithJsonDeserializer.class);
    // Uses custom deserializer which always returns `[3, 2, 1]`
    assertThat(deserialized.f).isEqualTo(Arrays.asList(3, 2, 1));

    // Verify that delegate serializer for List is used
    String json = gson.toJson(new WithJsonDeserializer(Arrays.asList(4, 5, 6)));
    assertThat(json).isEqualTo("{\"f\":[4,5,6]}");
  }

  private static class WithJsonDeserializer {
    @JsonAdapter(Deserializer.class)
    List<Integer> f;

    WithJsonDeserializer(List<Integer> f) {
      this.f = f;
    }

    static class Deserializer implements JsonDeserializer<List<Integer>> {
      @Override
      public List<Integer> deserialize(
          JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return Arrays.asList(3, 2, 1);
      }
    }
  }
}
