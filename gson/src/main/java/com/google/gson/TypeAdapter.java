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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import com.google.gson.internal.bind.JsonElementWriter;
import com.google.gson.internal.bind.JsonTreeReader;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Converts Java objects to and from JSON.
 *
 * <h3>Defining a type's JSON form</h3>
 * By default Gson converts application classes to JSON using its built-in type
 * adapters. If Gson's default JSON conversion isn't appropriate for a type,
 * extend this class to customize the conversion. Here's an example of a type
 * adapter for an (X,Y) coordinate point: <pre>   {@code
 *
 *   public class PointAdapter extends TypeAdapter<Point> {
 *     public Point read(JsonReader reader) throws IOException {
 *       if (reader.peek() == JsonToken.NULL) {
 *         reader.nextNull();
 *         return null;
 *       }
 *       String xy = reader.nextString();
 *       String[] parts = xy.split(",");
 *       int x = Integer.parseInt(parts[0]);
 *       int y = Integer.parseInt(parts[1]);
 *       return new Point(x, y);
 *     }
 *     public void write(JsonWriter writer, Point value) throws IOException {
 *       if (value == null) {
 *         writer.nullValue();
 *         return;
 *       }
 *       String xy = value.getX() + "," + value.getY();
 *       writer.value(xy);
 *     }
 *   }}</pre>
 * With this type adapter installed, Gson will convert {@code Points} to JSON as
 * strings like {@code "5,8"} rather than objects like {@code {"x":5,"y":8}}. In
 * this case the type adapter binds a rich Java class to a compact JSON value.
 *
 * <p>The {@link #read(JsonReader) read()} method must read exactly one value
 * and {@link #write(JsonWriter,Object) write()} must write exactly one value.
 * For primitive types this is means readers should make exactly one call to
 * {@code nextBoolean()}, {@code nextDouble()}, {@code nextInt()}, {@code
 * nextLong()}, {@code nextString()} or {@code nextNull()}. Writers should make
 * exactly one call to one of <code>value()</code> or <code>nullValue()</code>.
 * For arrays, type adapters should start with a call to {@code beginArray()},
 * convert all elements, and finish with a call to {@code endArray}. For
 * objects, they should start with {@code beginObject()}, convert the object,
 * and finish with {@code endObject()}. Failing to convert a value or converting
 * too many values may cause the application to crash.
 *
 * <p>Type adapters should be prepared to read null from the stream and write it
 * to the stream. Alternatively, they should use {@link #nullSafe()} method while
 * registering the type adapter with Gson. If your {@code Gson} instance
 * has been configured to {@link GsonBuilder#serializeNulls()}, these nulls will be
 * written to the final document. Otherwise the value (and the corresponding name
 * when writing to a JSON object) will be omitted automatically. In either case
 * your type adapter must handle null.
 *
 * <p>To use a custom type adapter with Gson, you must <i>register</i> it with a
 * {@link GsonBuilder}: <pre>   {@code
 *
 *   GsonBuilder builder = new GsonBuilder();
 *   builder.registerTypeAdapter(Point.class, new PointAdapter());
 *   // if PointAdapter didn't check for nulls in its read/write methods, you should instead use
 *   // builder.registerTypeAdapter(Point.class, new PointAdapter().nullSafe());
 *   ...
 *   Gson gson = builder.create();
 * }</pre>
 *
 * <h3>JSON Conversion</h3>
 * <p>A type adapter registered with Gson is automatically invoked while serializing
 * or deserializing JSON. However, you can also use type adapters directly to serialize
 * and deserialize JSON. Here is an example for deserialization: <pre>   {@code
 *
 *   String json = "{'origin':'0,0','points':['1,2','3,4']}";
 *   TypeAdapter<Graph> graphAdapter = gson.getAdapter(Graph.class);
 *   Graph graph = graphAdapter.fromJson(json);
 * }</pre>
 * And an example for serialization: <pre>   {@code
 *
 *   Graph graph = new Graph(...);
 *   TypeAdapter<Graph> graphAdapter = gson.getAdapter(Graph.class);
 *   String json = graphAdapter.toJson(graph);
 * }</pre>
 *
 * <p>Type adapters are <strong>type-specific</strong>. For example, a {@code
 * TypeAdapter<Date>} can convert {@code Date} instances to JSON and JSON to
 * instances of {@code Date}, but cannot convert any other types.
 *
 * @since 2.1
 */
public abstract class TypeAdapter<T> {

  /**
   * Writes one JSON value (an array, object, string, number, boolean or null)
   * for {@code value}.
   *
   * @param value the Java object to write. May be null.
   */
  public abstract void write(JsonWriter out, T value) throws IOException;

  /**
   * Converts {@code value} to a JSON document and writes it to {@code out}.
   * Unlike Gson's similar {@link Gson#toJson(JsonElement, Appendable) toJson}
   * method, this write is strict. Create a {@link
   * JsonWriter#setLenient(boolean) lenient} {@code JsonWriter} and call
   * {@link #write(com.google.gson.stream.JsonWriter, Object)} for lenient
   * writing.
   *
   * @param value the Java object to convert. May be null.
   */
  public final void toJson(Writer out, T value) throws IOException {
    JsonWriter writer = new JsonWriter(out);
    write(writer, value);
  }

  /**
   * This wrapper method is used to make a type adapter null tolerant. In general, a
   * type adapter is required to handle nulls in write and read methods. Here is how this
   * is typically done:<br>
   * <pre>   {@code
   *
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
   * You can avoid this boilerplate handling of nulls by wrapping your type adapter with
   * this method. Here is how we will rewrite the above example:
   * <pre>   {@code
   *
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
   * Note that we didn't need to check for nulls in our type adapter after we used nullSafe.
   */
  public TypeAdapter<T> nullSafe() {
    return new TypeAdapter<T>() {
      @Override public void write(JsonWriter out, T value) throws IOException {
        if (value == null) {
          out.nullValue();
        } else {
          TypeAdapter.this.write(out, value);
        }
      }
      @Override public T read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
          reader.nextNull();
          return null;
        }
        return TypeAdapter.this.read(reader);
      }
    };
  }

  /**
   * Converts {@code value} to a JSON document. Unlike Gson's similar {@link
   * Gson#toJson(Object) toJson} method, this write is strict. Create a {@link
   * JsonWriter#setLenient(boolean) lenient} {@code JsonWriter} and call
   * {@link #write(com.google.gson.stream.JsonWriter, Object)} for lenient
   * writing.
   *
   * @param value the Java object to convert. May be null.
   */
  public final String toJson(T value) throws IOException {
    StringWriter stringWriter = new StringWriter();
    toJson(stringWriter, value);
    return stringWriter.toString();
  }

  /**
   * Converts {@code value} to a JSON tree.
   *
   * @param value the Java object to convert. May be null.
   * @return the converted JSON tree. May be {@link JsonNull}.
   */
  public JsonElement toJsonTree(T value) {
    try {
      JsonElementWriter jsonWriter = new JsonElementWriter();
      jsonWriter.setLenient(true);
      write(jsonWriter, value);
      return jsonWriter.get();
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  /**
   * Reads one JSON value (an array, object, string, number, boolean or null)
   * and converts it to a Java object. Returns the converted object.
   *
   * @return the converted Java object. May be null.
   */
  public abstract T read(JsonReader in) throws IOException;

  /**
   * Converts the JSON document in {@code in} to a Java object. Unlike Gson's
   * similar {@link Gson#fromJson(java.io.Reader, Class) fromJson} method, this
   * read is strict. Create a {@link JsonReader#setLenient(boolean) lenient}
   * {@code JsonReader} and call {@link #read(JsonReader)} for lenient reading.
   *
   * @return the converted Java object. May be null.
   */
  public final T fromJson(Reader in) throws IOException {
    JsonReader reader = new JsonReader(in);
    reader.setLenient(true); // TODO: non-lenient?
    return read(reader);
  }

  /**
   * Converts the JSON document in {@code json} to a Java object. Unlike Gson's
   * similar {@link Gson#fromJson(String, Class) fromJson} method, this read is
   * strict. Create a {@link JsonReader#setLenient(boolean) lenient} {@code
   * JsonReader} and call {@link #read(JsonReader)} for lenient reading.
   *
   * @return the converted Java object. May be null.
   */
  public final T fromJson(String json) throws IOException {
    return fromJson(new StringReader(json));
  }

  /**
   * Converts {@code jsonTree} to a Java object.
   *
   * @param jsonTree the Java object to convert. May be {@link JsonNull}.
   */
  public T fromJsonTree(JsonElement jsonTree) {
    try {
      JsonReader jsonReader = new JsonTreeReader(jsonTree);
      jsonReader.setLenient(true);
      return read(jsonReader);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  /**
   * Creates type adapters for set of related types. Type adapter factories are
   * most useful when several types share similar structure in their JSON form.
   *
   * <h3>Example: Converting enums to lowercase</h3>
   * In this example, we implement a factory that creates type adapters for all
   * enums. The type adapters will write enums in lowercase, despite the fact
   * that they're defined in {@code CONSTANT_CASE} in the corresponding Java
   * model: <pre>   {@code
   *
   *   public class LowercaseEnumTypeAdapterFactory implements TypeAdapter.Factory {
   *     public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
   *       Class<T> rawType = (Class<T>) type.getRawType();
   *       if (!rawType.isEnum()) {
   *         return null;
   *       }
   *
   *       final Map<String, T> lowercaseToConstant = new HashMap<String, T>();
   *       for (T constant : rawType.getEnumConstants()) {
   *         lowercaseToConstant.put(toLowercase(constant), constant);
   *       }
   *
   *       return new TypeAdapter<T>() {
   *         public void write(JsonWriter out, T value) throws IOException {
   *           if (value == null) {
   *             out.nullValue();
   *           } else {
   *             out.value(toLowercase(value));
   *           }
   *         }
   *
   *         public T read(JsonReader reader) throws IOException {
   *           if (reader.peek() == JsonToken.NULL) {
   *             reader.nextNull();
   *             return null;
   *           } else {
   *             return lowercaseToConstant.get(reader.nextString());
   *           }
   *         }
   *       };
   *     }
   *
   *     private String toLowercase(Object o) {
   *       return o.toString().toLowerCase(Locale.US);
   *     }
   *   }
   * }</pre>
   *
   * <p>Type adapter factories select which types they provide type adapters
   * for. If a factory cannot support a given type, it must return null when
   * that type is passed to {@link #create}. Factories should expect {@code
   * create()} to be called on them for many types and should return null for
   * most of those types. In the above example the factory returns null for
   * calls to {@code create()} where {@code type} is not an enum.
   *
   * <p>A factory is typically called once per type, but the returned type
   * adapter may be used many times. It is most efficient to do expensive work
   * like reflection in {@code create()} so that the type adapter's {@code
   * read()} and {@code write()} methods can be very fast. In this example the
   * mapping from lowercase name to enum value is computed eagerly.
   *
   * <p>As with type adapters, factories must be <i>registered</i> with a {@link
   * GsonBuilder} for them to take effect: <pre>   {@code
   *
   *  GsonBuilder builder = new GsonBuilder();
   *  builder.registerTypeAdapterFactory(new LowercaseEnumTypeAdapterFactory());
   *  ...
   *  Gson gson = builder.create();
   * }</pre>
   * If multiple factories support the same type, the factory registered earlier
   * takes precedence.
   *
   * <h3>Example: composing other type adapters</h3>
   * In this example we implement a factory for Guava's {@code Multiset}
   * collection type. The factory can be used to create type adapters for
   * multisets of any element type: the type adapter for {@code
   * Multiset<String>} is different from the type adapter for {@code
   * Multiset<URL>}.
   *
   * <p>The type adapter <i>delegates</i> to another type adapter for the
   * multiset elements. It figures out the element type by reflecting on the
   * multiset's type token. A {@code Gson} is passed in to {@code create} for
   * just this purpose: <pre>   {@code
   *
   *   public class MultisetTypeAdapterFactory implements TypeAdapter.Factory {
   *     public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
   *       Type type = typeToken.getType();
   *       if (typeToken.getRawType() != Multiset.class
   *           || !(type instanceof ParameterizedType)) {
   *         return null;
   *       }
   *
   *       Type elementType = ((ParameterizedType) type).getActualTypeArguments()[0];
   *       TypeAdapter<?> elementAdapter = gson.getAdapter(TypeToken.get(elementType));
   *       return (TypeAdapter<T>) newMultisetAdapter(elementAdapter);
   *     }
   *
   *     private <E> TypeAdapter<Multiset<E>> newMultisetAdapter(
   *         final TypeAdapter<E> elementAdapter) {
   *       return new TypeAdapter<Multiset<E>>() {
   *         public void write(JsonWriter out, Multiset<E> value) throws IOException {
   *           if (value == null) {
   *             out.nullValue();
   *             return;
   *           }
   *
   *           out.beginArray();
   *           for (Multiset.Entry<E> entry : value.entrySet()) {
   *             out.value(entry.getCount());
   *             elementAdapter.write(out, entry.getElement());
   *           }
   *           out.endArray();
   *         }
   *
   *         public Multiset<E> read(JsonReader in) throws IOException {
   *           if (in.peek() == JsonToken.NULL) {
   *             in.nextNull();
   *             return null;
   *           }
   *
   *           Multiset<E> result = LinkedHashMultiset.create();
   *           in.beginArray();
   *           while (in.hasNext()) {
   *             int count = in.nextInt();
   *             E element = elementAdapter.read(in);
   *             result.add(element, count);
   *           }
   *           in.endArray();
   *           return result;
   *         }
   *       };
   *     }
   *   }
   * }</pre>
   * Delegating from one type adapter to another is extremely powerful; it's
   * the foundation of how Gson converts Java objects and collections. Whenever
   * possible your factory should retrieve its delegate type adapter in the
   * {@code create()} method; this ensures potentially-expensive type adapter
   * creation happens only once.
   *
   * @since 2.1
   */
  public interface Factory {

    /**
     * Returns a type adapter for {@code type}, or null if this factory doesn't
     * support {@code type}.
     */
    <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type);
  }
}
