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

/**
 * A class representing an element of Json. It could either be a {@link JsonObject}, a
 * {@link JsonArray}, a {@link JsonPrimitive} or a {@link JsonNull}.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public abstract class JsonElement {

  /**
   * provides check for verifying if this element is an array or not.
   *
   * @return true if this element is of type {@link JsonArray}, false otherwise.
   */
  public boolean isJsonArray() {
    return this instanceof JsonArray;
  }

  /**
   * provides check for verifying if this element is a Json object or not.
   *
   * @return true if this element is of type {@link JsonObject}, false otherwise.
   */
  public boolean isJsonObject() {
    return this instanceof JsonObject;
  }

  /**
   * provides check for verifying if this element is a primitive or not.
   *
   * @return true if this element is of type {@link JsonPrimitive}, false otherwise.
   */
  public boolean isJsonPrimitive() {
    return this instanceof JsonPrimitive;
  }

  /**
   * provides check for verifying if this element represents a null value or not.
   *
   * @return true if this element is of type {@link JsonNull}, false otherwise.
   */
  public boolean isJsonNull() {
    return this instanceof JsonNull;
  }

  /**
   * convenience method to get this element as a {@link JsonObject}. If the element is of some
   * other type, a {@link ClassCastException} will result. Hence it is best to use this method
   * after ensuring that this element is of the desired type by calling {@link #isJsonObject()}
   * first.
   *
   * @return get this element as a {@link JsonObject}.
   * @throws ClassCastException if the element is of another type.
   */
  public JsonObject getAsJsonObject() {
    return (JsonObject) this;
  }

  /**
   * convenience method to get this element as a {@link JsonArray}. If the element is of some
   * other type, a {@link ClassCastException} will result. Hence it is best to use this method
   * after ensuring that this element is of the desired type by calling {@link #isJsonArray()}
   * first.
   *
   * @return get this element as a {@link JsonArray}.
   * @throws ClassCastException if the element is of another type.
   */
  public JsonArray getAsJsonArray() {
    return (JsonArray) this;
  }

  /**
   * convenience method to get this element as a {@link JsonPrimitive}. If the element is of some
   * other type, a {@link ClassCastException} will result. Hence it is best to use this method
   * after ensuring that this element is of the desired type by calling {@link #isJsonPrimitive()}
   * first.
   *
   * @return get this element as a {@link JsonPrimitive}.
   * @throws ClassCastException if the element is of another type.
   */
  public JsonPrimitive getAsJsonPrimitive() {
    return (JsonPrimitive) this;
  }

  /**
   * convenience method to get this element as a {@link JsonNull}. If the element is of some
   * other type, a {@link ClassCastException} will result. Hence it is best to use this method
   * after ensuring that this element is of the desired type by calling {@link #isJsonNull()}
   * first.
   *
   * @return get this element as a {@link JsonNull}.
   * @throws ClassCastException if the element is of another type.
   */
  public JsonNull getAsJsonNull() {
    return (JsonNull) this;
  }

  /**
   * convenience method to get this element as a boolean value.
   *
   * @return get this element as a primitive boolean value.
   * @throws ClassCastException if the element is of not a {@link JsonPrimitive} and is not a valid
   * boolean value.
   * @throws IllegalStateException if the element is of the type {@link JsonArray} but contains
   * more than a single element.
   */
  public boolean getAsBoolean() {
    throw new UnsupportedOperationException();
  }

  /**
   * convenience method to get this element as a {@link Boolean} value.
   *
   * @return get this element as a {@link Boolean} value.
   * @throws ClassCastException if the element is of not a {@link JsonPrimitive} and is not a valid
   * boolean value.
   * @throws IllegalStateException if the element is of the type {@link JsonArray} but contains
   * more than a single element.
   */
  Boolean getAsBooleanWrapper() {
    throw new UnsupportedOperationException();
  }

  /**
   * convenience method to get this element as a {@link Number}.
   *
   * @return get this element as a {@link Number}.
   * @throws ClassCastException if the element is of not a {@link JsonPrimitive} and is not a valid
   * number.
   * @throws IllegalStateException if the element is of the type {@link JsonArray} but contains
   * more than a single element.
   */
  public Number getAsNumber() {
    throw new UnsupportedOperationException();
  }

  /**
   * convenience method to get this element as a string value.
   *
   * @return get this element as a string value.
   * @throws ClassCastException if the element is of not a {@link JsonPrimitive} and is not a valid
   * string value.
   * @throws IllegalStateException if the element is of the type {@link JsonArray} but contains
   * more than a single element.
   */
  public String getAsString() {
    throw new UnsupportedOperationException();
  }

  /**
   * convenience method to get this element as a primitive double value.
   *
   * @return get this element as a primitive double value.
   * @throws ClassCastException if the element is of not a {@link JsonPrimitive} and is not a valid
   * double value.
   * @throws IllegalStateException if the element is of the type {@link JsonArray} but contains
   * more than a single element.
   */
  public double getAsDouble() {
    throw new UnsupportedOperationException();
  }

  /**
   * convenience method to get this element as a primitive float value.
   *
   * @return get this element as a primitive float value.
   * @throws ClassCastException if the element is of not a {@link JsonPrimitive} and is not a valid
   * float value.
   * @throws IllegalStateException if the element is of the type {@link JsonArray} but contains
   * more than a single element.
   */
  public float getAsFloat() {
    throw new UnsupportedOperationException();
  }

  /**
   * convenience method to get this element as a primitive long value.
   *
   * @return get this element as a primitive long value.
   * @throws ClassCastException if the element is of not a {@link JsonPrimitive} and is not a valid
   * long value.
   * @throws IllegalStateException if the element is of the type {@link JsonArray} but contains
   * more than a single element.
   */
  public long getAsLong() {
    throw new UnsupportedOperationException();
  }

  /**
   * convenience method to get this element as a primitive integer value.
   *
   * @return get this element as a primitive integer value.
   * @throws ClassCastException if the element is of not a {@link JsonPrimitive} and is not a valid
   * integer value.
   * @throws IllegalStateException if the element is of the type {@link JsonArray} but contains
   * more than a single element.
   */
  public int getAsInt() {
    throw new UnsupportedOperationException();
  }

  /**
   * convenience method to get this element as a primitive short value.
   *
   * @return get this element as a primitive short value.
   * @throws ClassCastException if the element is of not a {@link JsonPrimitive} and is not a valid
   * short value.
   * @throws IllegalStateException if the element is of the type {@link JsonArray} but contains
   * more than a single element.
   */
  public short getAsShort() {
    throw new UnsupportedOperationException();
  }

  /**
   * convenience method to get this element as an {@link Object} value.
   *
   * @return get this element as an Object value.
   * @throws ClassCastException if the element is of not a {@link JsonPrimitive} and is not a valid
   * Object value.
   * @throws IllegalStateException if the element is of the type {@link JsonArray} but contains
   * more than a single element.
   */
  Object getAsObject() {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns a String representation of this element.
   *
   * @return String the string representation of this element. The output is valid Json.
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    toString(sb);
    return sb.toString();
  }

  protected abstract void toString(StringBuilder sb);
}
