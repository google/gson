/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gson.functional;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Functional tests for the RuntimeTypeAdapterFactory feature in extras.
 */
public final class RuntimeTypeAdapterFactoryFunctionalTest extends TestCase {

  private final Gson gson = new Gson();

  /**
   * This test also ensures that {@link TypeAdapterFactory} registered through {@link JsonAdapter}
   * work correctly for {@link Gson#getDelegateAdapter(TypeAdapterFactory, TypeToken)}.
   */
  public void testSubclassesAutomaticallySerialized() throws Exception {
    Shape shape = new Circle(25);
    String json = gson.toJson(shape);
    shape = gson.fromJson(json, Shape.class);
    assertEquals(25, ((Circle)shape).radius);

    shape = new Square(15);
    json = gson.toJson(shape);
    shape = gson.fromJson(json, Shape.class);
    assertEquals(15, ((Square)shape).side);
    assertEquals(ShapeType.SQUARE, shape.type);
  }

  @JsonAdapter(Shape.JsonAdapterFactory.class)
  static class Shape {
    final ShapeType type;
    Shape(ShapeType type) { this.type = type; }
    private static final class JsonAdapterFactory extends RuntimeTypeAdapterFactory<Shape> {
      public JsonAdapterFactory() {
        super(Shape.class, "type");
        registerSubtype(Circle.class, ShapeType.CIRCLE.toString());
        registerSubtype(Square.class, ShapeType.SQUARE.toString());
      }
    }
  }

  public enum ShapeType {
    SQUARE, CIRCLE
  }

  private static final class Circle extends Shape {
    final int radius;
    Circle(int radius) { super(ShapeType.CIRCLE); this.radius = radius; }
  }

  private static final class Square extends Shape {
    final int side;
    Square(int side) { super(ShapeType.SQUARE); this.side = side; }
  }

  // Copied from the extras package
  static class RuntimeTypeAdapterFactory<T> implements TypeAdapterFactory {
    private final Class<?> baseType;
    private final String typeFieldName;
    private final Map<String, Class<?>> labelToSubtype = new LinkedHashMap<String, Class<?>>();
    private final Map<Class<?>, String> subtypeToLabel = new LinkedHashMap<Class<?>, String>();

    protected RuntimeTypeAdapterFactory(Class<?> baseType, String typeFieldName) {
      if (typeFieldName == null || baseType == null) {
        throw new NullPointerException();
      }
      this.baseType = baseType;
      this.typeFieldName = typeFieldName;
    }

    /**
     * Creates a new runtime type adapter using for {@code baseType} using {@code
     * typeFieldName} as the type field name. Type field names are case sensitive.
     */
    public static <T> RuntimeTypeAdapterFactory<T> of(Class<T> baseType, String typeFieldName) {
      return new RuntimeTypeAdapterFactory<T>(baseType, typeFieldName);
    }

    /**
     * Creates a new runtime type adapter for {@code baseType} using {@code "type"} as
     * the type field name.
     */
    public static <T> RuntimeTypeAdapterFactory<T> of(Class<T> baseType) {
      return new RuntimeTypeAdapterFactory<T>(baseType, "type");
    }

    /**
     * Registers {@code type} identified by {@code label}. Labels are case
     * sensitive.
     *
     * @throws IllegalArgumentException if either {@code type} or {@code label}
     *     have already been registered on this type adapter.
     */
    public RuntimeTypeAdapterFactory<T> registerSubtype(Class<? extends T> type, String label) {
      if (type == null || label == null) {
        throw new NullPointerException();
      }
      if (subtypeToLabel.containsKey(type) || labelToSubtype.containsKey(label)) {
        throw new IllegalArgumentException("types and labels must be unique");
      }
      labelToSubtype.put(label, type);
      subtypeToLabel.put(type, label);
      return this;
    }

    /**
     * Registers {@code type} identified by its {@link Class#getSimpleName simple
     * name}. Labels are case sensitive.
     *
     * @throws IllegalArgumentException if either {@code type} or its simple name
     *     have already been registered on this type adapter.
     */
    public RuntimeTypeAdapterFactory<T> registerSubtype(Class<? extends T> type) {
      return registerSubtype(type, type.getSimpleName());
    }

    @Override public <R> TypeAdapter<R> create(Gson gson, TypeToken<R> type) {
      if (type.getRawType() != baseType) {
        return null;
      }

      final Map<String, TypeAdapter<?>> labelToDelegate
          = new LinkedHashMap<String, TypeAdapter<?>>();
      final Map<Class<?>, TypeAdapter<?>> subtypeToDelegate
          = new LinkedHashMap<Class<?>, TypeAdapter<?>>();
      for (Map.Entry<String, Class<?>> entry : labelToSubtype.entrySet()) {
        TypeAdapter<?> delegate = gson.getDelegateAdapter(this, TypeToken.get(entry.getValue()));
        labelToDelegate.put(entry.getKey(), delegate);
        subtypeToDelegate.put(entry.getValue(), delegate);
      }

      return new TypeAdapter<R>() {
        @Override public R read(JsonReader in) throws IOException {
          JsonElement jsonElement = Streams.parse(in);
          JsonElement labelJsonElement = jsonElement.getAsJsonObject().get(typeFieldName);
          if (labelJsonElement == null) {
            throw new JsonParseException("cannot deserialize " + baseType
                + " because it does not define a field named " + typeFieldName);
          }
          String label = labelJsonElement.getAsString();
          @SuppressWarnings("unchecked") // registration requires that subtype extends T
          TypeAdapter<R> delegate = (TypeAdapter<R>) labelToDelegate.get(label);
          if (delegate == null) {
            throw new JsonParseException("cannot deserialize " + baseType + " subtype named "
                + label + "; did you forget to register a subtype?");
          }
          return delegate.fromJsonTree(jsonElement);
        }

        @Override public void write(JsonWriter out, R value) throws IOException {
          Class<?> srcType = value.getClass();
          String label = subtypeToLabel.get(srcType);
          @SuppressWarnings("unchecked") // registration requires that subtype extends T
          TypeAdapter<R> delegate = (TypeAdapter<R>) subtypeToDelegate.get(srcType);
          if (delegate == null) {
            throw new JsonParseException("cannot serialize " + srcType.getName()
                + "; did you forget to register a subtype?");
          }
          JsonObject jsonObject = delegate.toJsonTree(value).getAsJsonObject();
          if (!jsonObject.has(typeFieldName)) {
            JsonObject clone = new JsonObject();
            clone.add(typeFieldName, new JsonPrimitive(label));
            for (Map.Entry<String, JsonElement> e : jsonObject.entrySet()) {
              clone.add(e.getKey(), e.getValue());
            }
            jsonObject = clone;
          }
          Streams.write(jsonObject, out);
        }
      };
    }
  }
}
