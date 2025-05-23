/*
 * Copyright (C) 2011 Google Inc.
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

package com.google.gson;

import com.google.gson.internal.Streams;
import com.google.gson.internal.bind.JsonTreeReader;
import com.google.gson.internal.bind.JsonTreeWriter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

/**
 * Converts Java objects to and from JSON.
 *
 * <h2>Defining a type's JSON form</h2>
 *
 * By default Gson converts application classes to JSON using its built-in type adapters. If Gson's
 * default JSON conversion isn't appropriate for a type, extend this class to customize the
 * conversion. Here's an example of a type adapter for an (X,Y) coordinate point:
 *
 * <pre>{@code
 * public class PointAdapter extends TypeAdapter<Point> {
 *   public Point read(JsonReader reader) throws IOException {
 *     if (reader.peek() == JsonToken.NULL) {
 *       reader.nextNull();
 *       return null;
 *     }
 *     String xy = reader.nextString();
 *     String[] parts = xy.split(",");
 *     int x = Integer.parseInt(parts[0]);
 *     int y = Integer.parseInt(parts[1]);
 *     return new Point(x, y);
 *   }
 *   public void write(JsonWriter writer, Point value) throws IOException {
 *     if (value == null) {
 *       writer.nullValue();
 *       return;
 *     }
 *     String xy = value.getX() + "," + value.getY();
 *     writer.value(xy);
 *   }
 * }
 * }</pre>
 *
 * With this type adapter installed, Gson will convert {@code Points} to JSON as strings like {@code
 * "5,8"} rather than objects like {@code {"x":5,"y":8}}. In this case the type adapter binds a rich
 * Java class to a compact JSON value.
 *
 * <p>The {@link #read(JsonReader) read()} method must read exactly one value and {@link
 * #write(JsonWriter,Object) write()} must write exactly one value. For primitive types this is
 * means readers should make exactly one call to {@code nextBoolean()}, {@code nextDouble()}, {@code
 * nextInt()}, {@code nextLong()}, {@code nextString()} or {@code nextNull()}. Writers should make
 * exactly one call to one of {@code value()} or {@code nullValue()}. For arrays, type adapters
 * should start with a call to {@code beginArray()}, convert all elements, and finish with a call to
 * {@code endArray()}. For objects, they should start with {@code beginObject()}, convert the
 * object, and finish with {@code endObject()}. Failing to convert a value or converting too many
 * values may cause the application to crash.
 *
 * <p>Type adapters should be prepared to read null from the stream and write it to the stream.
 * Alternatively, they should use {@link #nullSafe()} method while registering the type adapter with
 * Gson. If your {@code Gson} instance has been configured to {@link GsonBuilder#serializeNulls()},
 * these nulls will be written to the final document. Otherwise the value (and the corresponding
 * name when writing to a JSON object) will be omitted automatically. In either case your type
 * adapter must handle null.
 *
 * <p>Type adapters should be stateless and thread-safe, otherwise the thread-safety guarantees of
 * {@link Gson} might not apply.
 *
 * <p>To use a custom type adapter with Gson, you must <i>register</i> it with a {@link
 * GsonBuilder}:
 *
 * <pre>{@code
 * GsonBuilder builder = new GsonBuilder();
 * builder.registerTypeAdapter(Point.class, new PointAdapter());
 * // if PointAdapter didn't check for nulls in its read/write methods, you should instead use
 * // builder.registerTypeAdapter(Point.class, new PointAdapter().nullSafe());
 * ...
 * Gson gson = builder.create();
 * }</pre>
 *
 * @since 2.1
 */
// non-Javadoc:
//
// <h2>JSON Conversion</h2>
// <p>A type adapter registered with Gson is automatically invoked while serializing
// or deserializing JSON. However, you can also use type adapters directly to serialize
// and deserialize JSON. Here is an example for deserialization: <pre>{@code
//   String json = "{'origin':'0,0','points':['1,2','3,4']}";
//   TypeAdapter<Graph> graphAdapter = gson.getAdapter(Graph.class);
//   Graph graph = graphAdapter.fromJson(json);
// }</pre>
// And an example for serialization: <pre>{@code
//   Graph graph = new Graph(...);
//   TypeAdapter<Graph> graphAdapter = gson.getAdapter(Graph.class);
//   String json = graphAdapter.toJson(graph);
// }</pre>
//
// <p>Type adapters are <strong>type-specific</strong>. For example, a {@code
// TypeAdapter<Date>} can convert {@code Date} instances to JSON and JSON to
// instances of {@code Date}, but cannot convert any other types.
//
public abstract class TypeAdapter<T> {

  public TypeAdapter() {}

  /**
   * Writes one JSON value (an array, object, string, number, boolean or null) for {@code value}.
   *
   * @param value the Java object to write. May be null.
   */
  public abstract void write(JsonWriter out, T value) throws IOException;

  /**
   * Converts {@code value} to a JSON document and writes it to {@code out}.
   *
   * <p>A {@link JsonWriter} with default configuration is used for writing the JSON data. To
   * customize this behavior, create a {@link JsonWriter}, configure it and then use {@link
   * #write(JsonWriter, Object)} instead.
   *
   * @param value the Java object to convert. May be {@code null}.
   * @since 2.2
   */
  public final void toJson(Writer out, T value) throws IOException {
    JsonWriter writer = new JsonWriter(out);
    write(writer, value);
  }

  /**
   * Converts {@code value} to a JSON document.
   *
   * <p>A {@link JsonWriter} with default configuration is used for writing the JSON data. To
   * customize this behavior, create a {@link JsonWriter}, configure it and then use {@link
   * #write(JsonWriter, Object)} instead.
   *
   * @throws JsonIOException wrapping {@code IOException}s thrown by {@link #write(JsonWriter,
   *     Object)}
   * @param value the Java object to convert. May be {@code null}.
   * @since 2.2
   */
  public final String toJson(T value) {
    StringBuilder stringBuilder = new StringBuilder();
    try {
      toJson(Streams.writerForAppendable(stringBuilder), value);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
    return stringBuilder.toString();
  }

  /**
   * Converts {@code value} to a JSON tree.
   *
   * @param value the Java object to convert. May be {@code null}.
   * @return the converted JSON tree. May be {@link JsonNull}.
   * @throws JsonIOException wrapping {@code IOException}s thrown by {@link #write(JsonWriter,
   *     Object)}
   * @since 2.2
   */
  public final JsonElement toJsonTree(T value) {
    try {
      JsonTreeWriter jsonWriter = new JsonTreeWriter();
      write(jsonWriter, value);
      return jsonWriter.get();
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  /**
   * Reads one JSON value (an array, object, string, number, boolean or null) and converts it to a
   * Java object. Returns the converted object.
   *
   * @return the converted Java object. May be {@code null}.
   */
  public abstract T read(JsonReader in) throws IOException;

  /**
   * Converts the JSON document in {@code in} to a Java object.
   *
   * <p>A {@link JsonReader} with default configuration (that is with {@link
   * Strictness#LEGACY_STRICT} as strictness) is used for reading the JSON data. To customize this
   * behavior, create a {@link JsonReader}, configure it and then use {@link #read(JsonReader)}
   * instead.
   *
   * <p>No exception is thrown if the JSON data has multiple top-level JSON elements, or if there is
   * trailing data.
   *
   * @return the converted Java object. May be {@code null}.
   * @since 2.2
   */
  public final T fromJson(Reader in) throws IOException {
    JsonReader reader = new JsonReader(in);
    return read(reader);
  }

  /**
   * Converts the JSON document in {@code json} to a Java object.
   *
   * <p>A {@link JsonReader} with default configuration (that is with {@link
   * Strictness#LEGACY_STRICT} as strictness) is used for reading the JSON data. To customize this
   * behavior, create a {@link JsonReader}, configure it and then use {@link #read(JsonReader)}
   * instead.
   *
   * <p>No exception is thrown if the JSON data has multiple top-level JSON elements, or if there is
   * trailing data.
   *
   * @return the converted Java object. May be {@code null}.
   * @since 2.2
   */
  public final T fromJson(String json) throws IOException {
    return fromJson(new StringReader(json));
  }

  /**
   * Converts {@code jsonTree} to a Java object.
   *
   * @param jsonTree the JSON element to convert. May be {@link JsonNull}.
   * @return the converted Java object. May be {@code null}.
   * @throws JsonIOException wrapping {@code IOException}s thrown by {@link #read(JsonReader)}
   * @since 2.2
   */
  public final T fromJsonTree(JsonElement jsonTree) {
    try {
      JsonReader jsonReader = new JsonTreeReader(jsonTree);
      return read(jsonReader);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  /**
   * This wrapper method is used to make a type adapter null tolerant. In general, a type adapter is
   * required to handle nulls in write and read methods. Here is how this is typically done:<br>
   *
   * <pre>{@code
   * Gson gson = new GsonBuilder().registerTypeAdapter(Foo.class,
   *   new TypeAdapter<Foo>() {
   *     public Foo read(JsonReader in) throws IOException {
   *       if (in.peek() == JsonToken.NULL) {
   *         in.nextNull();
   *         return null;
   *       }
   *       // read a Foo from in and return it
   *     }
   *     public void write(JsonWriter out, Foo src) throws IOException {
   *       if (src == null) {
   *         out.nullValue();
   *         return;
   *       }
   *       // write src as JSON to out
   *     }
   *   }).create();
   * }</pre>
   *
   * You can avoid this boilerplate handling of nulls by wrapping your type adapter with this
   * method. Here is how we will rewrite the above example:
   *
   * <pre>{@code
   * Gson gson = new GsonBuilder().registerTypeAdapter(Foo.class,
   *   new TypeAdapter<Foo>() {
   *     public Foo read(JsonReader in) throws IOException {
   *       // read a Foo from in and return it
   *     }
   *     public void write(JsonWriter out, Foo src) throws IOException {
   *       // write src as JSON to out
   *     }
   *   }.nullSafe()).create();
   * }</pre>
   *
   * Note that we didn't need to check for nulls in our type adapter after we used nullSafe.
   */
  public final TypeAdapter<T> nullSafe() {
    if (!(this instanceof TypeAdapter.NullSafeTypeAdapter)) {
      return new NullSafeTypeAdapter();
    }
    return this;
  }

  private final class NullSafeTypeAdapter extends TypeAdapter<T> {
    @Override
    public void write(JsonWriter out, T value) throws IOException {
      if (value == null) {
        out.nullValue();
      } else {
        TypeAdapter.this.write(out, value);
      }
    }

    @Override
    public T read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull();
        return null;
      }
      return TypeAdapter.this.read(reader);
    }

    @Override
    public String toString() {
      return "NullSafeTypeAdapter[" + TypeAdapter.this + "]";
    }
  }
}
