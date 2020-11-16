package com.google.gson;

import com.google.gson.stream.JsonWriter;

/**
 * A class representing a raw JSON value. This should be used with
 * {@link JsonWriter#jsonValue(String)} and should not be encoded or escaped further.
 *
 * @author Zac Sweers
 * @since 2.8.7
 */
public final class JsonValue extends JsonElement {

  private final String jsonValue;

  public JsonValue(String jsonValue) {
    this.jsonValue = jsonValue;
  }

  /**
   * @return the underlying raw JSON value.
   */
  public String getJsonValue() {
    return jsonValue;
  }

  /**
   * Returns the same value as this is immutable.
   * @since 2.8.7
   */
  @Override public JsonElement deepCopy() {
    return this;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    JsonValue jsonValue1 = (JsonValue) o;

    return jsonValue.equals(jsonValue1.jsonValue);
  }

  @Override public int hashCode() {
    return jsonValue.hashCode();
  }

  @Override public String toString() {
    return "JsonValue{" + "jsonValue='" + jsonValue + '\'' + '}';
  }
}
