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

package com.google.gson;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A class representing an element of JSON. It could either be a {@link JsonObject}, a {@link
 * JsonArray}, a {@link JsonPrimitive} or a {@link JsonNull}.
 *
 * <p>This class provides multiple {@code getAs} methods which allow
 *
 * <ul>
 *   <li>obtaining the represented primitive value, for example {@link #getAsString()}
 *   <li>casting to the {@code JsonElement} subclasses in a convenient way, for example {@link
 *       #getAsJsonObject()}
 * </ul>
 *
 * <h2>Converting {@code JsonElement} from / to JSON</h2>
 *
 * There are two ways to parse JSON data as a {@link JsonElement}:
 *
 * <ul>
 *   <li>{@link JsonParser}, for example:
 *       <pre>
 * JsonObject jsonObject = JsonParser.parseString("{}").getAsJsonObject();
 * </pre>
 *   <li>{@link Gson#fromJson(Reader, Class) Gson.fromJson(..., JsonElement.class)}<br>
 *       It is possible to directly specify a {@code JsonElement} subclass, for example:
 *       <pre>
 * JsonObject jsonObject = gson.fromJson("{}", JsonObject.class);
 * </pre>
 * </ul>
 *
 * To convert a {@code JsonElement} to JSON either {@link #toString() JsonElement.toString()} or the
 * method {@link Gson#toJson(JsonElement)} and its overloads can be used.
 *
 * <p>It is also possible to obtain the {@link TypeAdapter} for {@code JsonElement} from a {@link
 * Gson} instance and then use it for conversion from and to JSON:
 *
 * <pre>{@code
 * TypeAdapter<JsonElement> adapter = gson.getAdapter(JsonElement.class);
 *
 * JsonElement value = adapter.fromJson("{}");
 * String json = adapter.toJson(value);
 * }</pre>
 *
 * <h2>{@code JsonElement} as JSON data</h2>
 *
 * {@code JsonElement} can also be treated as JSON data, allowing to deserialize from a {@code
 * JsonElement} and serializing to a {@code JsonElement}. The {@link Gson} class offers these
 * methods for this:
 *
 * <ul>
 *   <li>{@link Gson#fromJson(JsonElement, Class) Gson.fromJson(JsonElement, ...)}, for example:
 *       <pre>
 * JsonObject jsonObject = ...;
 * MyClass myObj = gson.fromJson(jsonObject, MyClass.class);
 * </pre>
 *   <li>{@link Gson#toJsonTree(Object)}, for example:
 *       <pre>
 * MyClass myObj = ...;
 * JsonElement json = gson.toJsonTree(myObj);
 * </pre>
 * </ul>
 *
 * The {@link TypeAdapter} class provides corresponding methods as well:
 *
 * <ul>
 *   <li>{@link TypeAdapter#fromJsonTree(JsonElement)}
 *   <li>{@link TypeAdapter#toJsonTree(Object)}
 * </ul>
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public abstract class JsonElement {
  /**
   * @deprecated Creating custom {@code JsonElement} subclasses is highly discouraged and can lead
   *     to undefined behavior.<br>
   *     This constructor is only kept for backward compatibility.
   */
  @Deprecated
  public JsonElement() {}

  /**
   * Returns a deep copy of this element. Immutable elements like primitives and nulls are not
   * copied.
   *
   * @since 2.8.2
   */
  public abstract JsonElement deepCopy();

  /**
   * Provides a check for verifying if this element is a JSON array or not.
   *
   * @return true if this element is of type {@link JsonArray}, false otherwise.
   */
  public boolean isJsonArray() {
    return this instanceof JsonArray;
  }

  /**
   * Provides a check for verifying if this element is a JSON object or not.
   *
   * @return true if this element is of type {@link JsonObject}, false otherwise.
   */
  public boolean isJsonObject() {
    return this instanceof JsonObject;
  }

  /**
   * Provides a check for verifying if this element is a primitive or not.
   *
   * @return true if this element is of type {@link JsonPrimitive}, false otherwise.
   */
  public boolean isJsonPrimitive() {
    return this instanceof JsonPrimitive;
  }

  /**
   * Provides a check for verifying if this element represents a null value or not.
   *
   * @return true if this element is of type {@link JsonNull}, false otherwise.
   * @since 1.2
   */
  public boolean isJsonNull() {
    return this instanceof JsonNull;
  }

  /**
   * Convenience method to get this element as a {@link JsonObject}. If this element is of some
   * other type, an {@link IllegalStateException} will result. Hence it is best to use this method
   * after ensuring that this element is of the desired type by calling {@link #isJsonObject()}
   * first.
   *
   * @return this element as a {@link JsonObject}.
   * @throws IllegalStateException if this element is of another type.
   */
  public JsonObject getAsJsonObject() {
    if (isJsonObject()) {
      return (JsonObject) this;
    }
    throw new IllegalStateException("Not a JSON Object: " + this);
  }

  /**
   * Convenience method to get this element as a {@link JsonArray}. If this element is of some other
   * type, an {@link IllegalStateException} will result. Hence it is best to use this method after
   * ensuring that this element is of the desired type by calling {@link #isJsonArray()} first.
   *
   * @return this element as a {@link JsonArray}.
   * @throws IllegalStateException if this element is of another type.
   */
  public JsonArray getAsJsonArray() {
    if (isJsonArray()) {
      return (JsonArray) this;
    }
    throw new IllegalStateException("Not a JSON Array: " + this);
  }

  /**
   * Convenience method to get this element as a {@link JsonPrimitive}. If this element is of some
   * other type, an {@link IllegalStateException} will result. Hence it is best to use this method
   * after ensuring that this element is of the desired type by calling {@link #isJsonPrimitive()}
   * first.
   *
   * @return this element as a {@link JsonPrimitive}.
   * @throws IllegalStateException if this element is of another type.
   */
  public JsonPrimitive getAsJsonPrimitive() {
    if (isJsonPrimitive()) {
      return (JsonPrimitive) this;
    }
    throw new IllegalStateException("Not a JSON Primitive: " + this);
  }

  /**
   * Convenience method to get this element as a {@link JsonNull}. If this element is of some other
   * type, an {@link IllegalStateException} will result. Hence it is best to use this method after
   * ensuring that this element is of the desired type by calling {@link #isJsonNull()} first.
   *
   * @return this element as a {@link JsonNull}.
   * @throws IllegalStateException if this element is of another type.
   * @since 1.2
   */
  @CanIgnoreReturnValue // When this method is used only to verify that the value is JsonNull
  public JsonNull getAsJsonNull() {
    if (isJsonNull()) {
      return (JsonNull) this;
    }
    throw new IllegalStateException("Not a JSON Null: " + this);
  }

  /**
   * Convenience method to get this element as a boolean value.
   *
   * @return this element as a primitive boolean value.
   * @throws UnsupportedOperationException if this element is not a {@link JsonPrimitive} or {@link
   *     JsonArray}.
   * @throws IllegalStateException if this element is of the type {@link JsonArray} but contains
   *     more than a single element.
   */
  public boolean getAsBoolean() {
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  /**
   * Convenience method to get this element as a {@link Number}.
   *
   * @return this element as a {@link Number}.
   * @throws UnsupportedOperationException if this element is not a {@link JsonPrimitive} or {@link
   *     JsonArray}, or cannot be converted to a number.
   * @throws IllegalStateException if this element is of the type {@link JsonArray} but contains
   *     more than a single element.
   */
  public Number getAsNumber() {
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  /**
   * Convenience method to get this element as a string value.
   *
   * @return this element as a string value.
   * @throws UnsupportedOperationException if this element is not a {@link JsonPrimitive} or {@link
   *     JsonArray}.
   * @throws IllegalStateException if this element is of the type {@link JsonArray} but contains
   *     more than a single element.
   */
  public String getAsString() {
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  /**
   * Convenience method to get this element as a primitive double value.
   *
   * @return this element as a primitive double value.
   * @throws UnsupportedOperationException if this element is not a {@link JsonPrimitive} or {@link
   *     JsonArray}.
   * @throws NumberFormatException if the value contained is not a valid double.
   * @throws IllegalStateException if this element is of the type {@link JsonArray} but contains
   *     more than a single element.
   */
  public double getAsDouble() {
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  /**
   * Convenience method to get this element as a primitive float value.
   *
   * @return this element as a primitive float value.
   * @throws UnsupportedOperationException if this element is not a {@link JsonPrimitive} or {@link
   *     JsonArray}.
   * @throws NumberFormatException if the value contained is not a valid float.
   * @throws IllegalStateException if this element is of the type {@link JsonArray} but contains
   *     more than a single element.
   */
  public float getAsFloat() {
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  /**
   * Convenience method to get this element as a primitive long value.
   *
   * @return this element as a primitive long value.
   * @throws UnsupportedOperationException if this element is not a {@link JsonPrimitive} or {@link
   *     JsonArray}.
   * @throws NumberFormatException if the value contained is not a valid long.
   * @throws IllegalStateException if this element is of the type {@link JsonArray} but contains
   *     more than a single element.
   */
  public long getAsLong() {
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  /**
   * Convenience method to get this element as a primitive integer value.
   *
   * @return this element as a primitive integer value.
   * @throws UnsupportedOperationException if this element is not a {@link JsonPrimitive} or {@link
   *     JsonArray}.
   * @throws NumberFormatException if the value contained is not a valid integer.
   * @throws IllegalStateException if this element is of the type {@link JsonArray} but contains
   *     more than a single element.
   */
  public int getAsInt() {
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  /**
   * Convenience method to get this element as a primitive byte value.
   *
   * @return this element as a primitive byte value.
   * @throws UnsupportedOperationException if this element is not a {@link JsonPrimitive} or {@link
   *     JsonArray}.
   * @throws NumberFormatException if the value contained is not a valid byte.
   * @throws IllegalStateException if this element is of the type {@link JsonArray} but contains
   *     more than a single element.
   * @since 1.3
   */
  public byte getAsByte() {
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  /**
   * Convenience method to get the first character of the string value of this element.
   *
   * @return the first character of the string value.
   * @throws UnsupportedOperationException if this element is not a {@link JsonPrimitive} or {@link
   *     JsonArray}, or if its string value is empty.
   * @throws IllegalStateException if this element is of the type {@link JsonArray} but contains
   *     more than a single element.
   * @since 1.3
   * @deprecated This method is misleading, as it does not get this element as a char but rather as
   *     a string's first character.
   */
  @Deprecated
  public char getAsCharacter() {
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  /**
   * Convenience method to get this element as a {@link BigDecimal}.
   *
   * @return this element as a {@link BigDecimal}.
   * @throws UnsupportedOperationException if this element is not a {@link JsonPrimitive} or {@link
   *     JsonArray}.
   * @throws NumberFormatException if this element is not a valid {@link BigDecimal}.
   * @throws IllegalStateException if this element is of the type {@link JsonArray} but contains
   *     more than a single element.
   * @since 1.2
   */
  public BigDecimal getAsBigDecimal() {
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  /**
   * Convenience method to get this element as a {@link BigInteger}.
   *
   * @return this element as a {@link BigInteger}.
   * @throws UnsupportedOperationException if this element is not a {@link JsonPrimitive} or {@link
   *     JsonArray}.
   * @throws NumberFormatException if this element is not a valid {@link BigInteger}.
   * @throws IllegalStateException if this element is of the type {@link JsonArray} but contains
   *     more than a single element.
   * @since 1.2
   */
  public BigInteger getAsBigInteger() {
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  /**
   * Convenience method to get this element as a primitive short value.
   *
   * @return this element as a primitive short value.
   * @throws UnsupportedOperationException if this element is not a {@link JsonPrimitive} or {@link
   *     JsonArray}.
   * @throws NumberFormatException if the value contained is not a valid short.
   * @throws IllegalStateException if this element is of the type {@link JsonArray} but contains
   *     more than a single element.
   */
  public short getAsShort() {
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  /**
   * Converts this element to a JSON string.
   *
   * <p>For example:
   *
   * <pre>
   * JsonObject object = new JsonObject();
   * object.add("a", JsonNull.INSTANCE);
   * JsonArray array = new JsonArray();
   * array.add(1);
   * object.add("b", array);
   *
   * String json = object.toString();
   * // json: {"a":null,"b":[1]}
   * </pre>
   *
   * If this element or any nested elements contain {@link Double#NaN NaN} or {@link
   * Double#isInfinite() Infinity} that value is written to JSON, even though the JSON specification
   * does not permit these values.
   *
   * <p>To customize formatting or to directly write to an {@link Appendable} instead of creating an
   * intermediate {@code String} first, use {@link Gson#toJson(JsonElement, Appendable)
   * Gson.toJson(JsonElement, ...)}.
   *
   * <p>To get the contained String value (without enclosing {@code "} and without escaping), use
   * {@link #getAsString()} instead:
   *
   * <pre>
   * JsonPrimitive jsonPrimitive = new JsonPrimitive("with \" quote");
   * String json = jsonPrimitive.toString();
   * // json: "with \" quote"
   * String value = jsonPrimitive.getAsString();
   * // value: with " quote
   * </pre>
   */
  @Override
  public String toString() {
    try {
      StringBuilder stringBuilder = new StringBuilder();
      JsonWriter jsonWriter = new JsonWriter(Streams.writerForAppendable(stringBuilder));
      // Make writer lenient because toString() must not fail, even if for example JsonPrimitive
      // contains NaN
      jsonWriter.setStrictness(Strictness.LENIENT);
      Streams.write(this, jsonWriter);
      return stringBuilder.toString();
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }
}
