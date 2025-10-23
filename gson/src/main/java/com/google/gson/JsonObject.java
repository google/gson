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
import com.google.gson.internal.LinkedTreeMap;
import java.util.Map;
import java.util.Set;

/**
 * A class representing an object type in JSON. An object consists of name-value pairs where names
 * are strings, and values are any other type of {@link JsonElement}. This allows for a creating a
 * tree of JsonElements. The member elements of this object are maintained in order they were added.
 * This class does not support {@code null} values. If {@code null} is provided as value argument to
 * any of the methods, it is converted to a {@link JsonNull}.
 *
 * <p>{@code JsonObject} does not implement the {@link Map} interface, but a {@code Map} view of it
 * can be obtained with {@link #asMap()}.
 *
 * <p>See the {@link JsonElement} documentation for details on how to convert {@code JsonObject} and
 * generally any {@code JsonElement} from and to JSON.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public final class JsonObject extends JsonElement {
  private final LinkedTreeMap<String, JsonElement> members = new LinkedTreeMap<>(false);

  /** Creates an empty JsonObject. */
  @SuppressWarnings("deprecation") // superclass constructor
  public JsonObject() {}

  /**
   * Creates a JsonObject with the specified member.
   *
   * @return a new JsonObject.
   * @since $next-version$
   */
  public static JsonObject of(String key, JsonElement value) {
    JsonObject object = new JsonObject();
    object.add(key, value);
    return object;
  }

  /**
   * Creates a JsonObject with the specified members.
   *
   * @return a new JsonObject.
   * @since $next-version$
   */
  public static JsonObject of(String key1, JsonElement value1, String key2, JsonElement value2) {
    JsonObject object = new JsonObject();
    object.add(key1, value1);
    object.add(key2, value2);
    return object;
  }

  /**
   * Creates a JsonObject with the specified members.
   *
   * @return a new JsonObject.
   * @since $next-version$
   */
  public static JsonObject of(
      String key1,
      JsonElement value1,
      String key2,
      JsonElement value2,
      String key3,
      JsonElement value3) {
    JsonObject object = new JsonObject();
    object.add(key1, value1);
    object.add(key2, value2);
    object.add(key3, value3);
    return object;
  }

  /**
   * Creates a JsonObject with the specified members.
   *
   * @return a new JsonObject.
   * @since $next-version$
   */
  public static JsonObject of(
      String key1,
      JsonElement value1,
      String key2,
      JsonElement value2,
      String key3,
      JsonElement value3,
      String key4,
      JsonElement value4) {
    JsonObject object = new JsonObject();
    object.add(key1, value1);
    object.add(key2, value2);
    object.add(key3, value3);
    object.add(key4, value4);
    return object;
  }

  /**
   * Creates a JsonObject with the specified members.
   *
   * @return a new JsonObject.
   * @since $next-version$
   */
  public static JsonObject of(
      String key1,
      JsonElement value1,
      String key2,
      JsonElement value2,
      String key3,
      JsonElement value3,
      String key4,
      JsonElement value4,
      String key5,
      JsonElement value5) {
    JsonObject object = new JsonObject();
    object.add(key1, value1);
    object.add(key2, value2);
    object.add(key3, value3);
    object.add(key4, value4);
    object.add(key5, value5);
    return object;
  }

  /**
   * Creates a JsonObject with the specified members.
   *
   * @return a new JsonObject.
   * @since $next-version$
   */
  public static JsonObject of(
      String key1,
      JsonElement value1,
      String key2,
      JsonElement value2,
      String key3,
      JsonElement value3,
      String key4,
      JsonElement value4,
      String key5,
      JsonElement value5,
      String key6,
      JsonElement value6) {
    JsonObject object = new JsonObject();
    object.add(key1, value1);
    object.add(key2, value2);
    object.add(key3, value3);
    object.add(key4, value4);
    object.add(key5, value5);
    object.add(key6, value6);
    return object;
  }

  /**
   * Creates a JsonObject with the specified members.
   *
   * @return a new JsonObject.
   * @since $next-version$
   */
  public static JsonObject of(
      String key1,
      JsonElement value1,
      String key2,
      JsonElement value2,
      String key3,
      JsonElement value3,
      String key4,
      JsonElement value4,
      String key5,
      JsonElement value5,
      String key6,
      JsonElement value6,
      String key7,
      JsonElement value7) {
    JsonObject object = new JsonObject();
    object.add(key1, value1);
    object.add(key2, value2);
    object.add(key3, value3);
    object.add(key4, value4);
    object.add(key5, value5);
    object.add(key6, value6);
    object.add(key7, value7);
    return object;
  }

  /**
   * Creates a JsonObject with the specified members.
   *
   * @return a new JsonObject.
   * @since $next-version$
   */
  public static JsonObject of(
      String key1,
      JsonElement value1,
      String key2,
      JsonElement value2,
      String key3,
      JsonElement value3,
      String key4,
      JsonElement value4,
      String key5,
      JsonElement value5,
      String key6,
      JsonElement value6,
      String key7,
      JsonElement value7,
      String key8,
      JsonElement value8) {
    JsonObject object = new JsonObject();
    object.add(key1, value1);
    object.add(key2, value2);
    object.add(key3, value3);
    object.add(key4, value4);
    object.add(key5, value5);
    object.add(key6, value6);
    object.add(key7, value7);
    object.add(key8, value8);
    return object;
  }

  /**
   * Creates a JsonObject with the specified members.
   *
   * @return a new JsonObject.
   * @since $next-version$
   */
  public static JsonObject of(
      String key1,
      JsonElement value1,
      String key2,
      JsonElement value2,
      String key3,
      JsonElement value3,
      String key4,
      JsonElement value4,
      String key5,
      JsonElement value5,
      String key6,
      JsonElement value6,
      String key7,
      JsonElement value7,
      String key8,
      JsonElement value8,
      String key9,
      JsonElement value9) {
    JsonObject object = new JsonObject();
    object.add(key1, value1);
    object.add(key2, value2);
    object.add(key3, value3);
    object.add(key4, value4);
    object.add(key5, value5);
    object.add(key6, value6);
    object.add(key7, value7);
    object.add(key8, value8);
    object.add(key9, value9);
    return object;
  }

  /**
   * Creates a JsonObject with the specified members.
   *
   * @return a new JsonObject.
   * @since $next-version$
   */
  public static JsonObject of(
      String key1,
      JsonElement value1,
      String key2,
      JsonElement value2,
      String key3,
      JsonElement value3,
      String key4,
      JsonElement value4,
      String key5,
      JsonElement value5,
      String key6,
      JsonElement value6,
      String key7,
      JsonElement value7,
      String key8,
      JsonElement value8,
      String key9,
      JsonElement value9,
      String key10,
      JsonElement value10) {
    JsonObject object = new JsonObject();
    object.add(key1, value1);
    object.add(key2, value2);
    object.add(key3, value3);
    object.add(key4, value4);
    object.add(key5, value5);
    object.add(key6, value6);
    object.add(key7, value7);
    object.add(key8, value8);
    object.add(key9, value9);
    object.add(key10, value10);
    return object;
  }

  /**
   * Creates a JsonObject with the specified entries.
   *
   * @return a new JsonObject with given entries.
   * @since $next-version$
   */
  @SafeVarargs
  public static JsonObject ofEntries(Map.Entry<String, ? extends JsonElement>... entries) {
    JsonObject object = new JsonObject();
    for (Map.Entry<String, ? extends JsonElement> entry : entries) {
      object.add(entry.getKey(), entry.getValue());
    }
    return object;
  }

  /**
   * Creates a JsonObject with the specified entries.
   *
   * @return a new JsonObject with given entries.
   * @since $next-version$
   */
  public static JsonObject ofEntries(
      Iterable<? extends Map.Entry<String, ? extends JsonElement>> entries) {
    JsonObject object = new JsonObject();
    for (Map.Entry<String, ? extends JsonElement> entry : entries) {
      object.add(entry.getKey(), entry.getValue());
    }
    return object;
  }

  /**
   * Creates a JsonObject from the specified map. All entries of the map will be shallow copied into
   * the new JsonObject.
   *
   * @return a new JsonObject with all entries of the given map.
   * @since $next-version$
   */
  public static JsonObject copyOf(Map<String, ? extends JsonElement> map) {
    return ofEntries(map.entrySet());
  }

  /**
   * Creates a deep copy of this element and all its children.
   *
   * @since 2.8.2
   */
  @Override
  public JsonObject deepCopy() {
    JsonObject result = new JsonObject();
    for (Map.Entry<String, JsonElement> entry : members.entrySet()) {
      result.add(entry.getKey(), entry.getValue().deepCopy());
    }
    return result;
  }

  /**
   * Adds a member, which is a name-value pair, to self. The name must be a String, but the value
   * can be an arbitrary {@link JsonElement}, thereby allowing you to build a full tree of
   * JsonElements rooted at this node.
   *
   * @param property name of the member.
   * @param value the member object.
   */
  public void add(String property, JsonElement value) {
    members.put(property, value == null ? JsonNull.INSTANCE : value);
  }

  /**
   * Removes the {@code property} from this object.
   *
   * @param property name of the member that should be removed.
   * @return the {@link JsonElement} object that is being removed, or {@code null} if no member with
   *     this name exists.
   * @since 1.3
   */
  @CanIgnoreReturnValue
  public JsonElement remove(String property) {
    return members.remove(property);
  }

  /**
   * Convenience method to add a string member. The specified value is converted to a {@link
   * JsonPrimitive} of String.
   *
   * @param property name of the member.
   * @param value the string value associated with the member.
   */
  public void addProperty(String property, String value) {
    add(property, value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
  }

  /**
   * Convenience method to add a number member. The specified value is converted to a {@link
   * JsonPrimitive} of Number.
   *
   * @param property name of the member.
   * @param value the number value associated with the member.
   */
  public void addProperty(String property, Number value) {
    add(property, value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
  }

  /**
   * Convenience method to add a boolean member. The specified value is converted to a {@link
   * JsonPrimitive} of Boolean.
   *
   * @param property name of the member.
   * @param value the boolean value associated with the member.
   */
  public void addProperty(String property, Boolean value) {
    add(property, value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
  }

  /**
   * Convenience method to add a char member. The specified value is converted to a {@link
   * JsonPrimitive} of Character.
   *
   * @param property name of the member.
   * @param value the char value associated with the member.
   */
  public void addProperty(String property, Character value) {
    add(property, value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
  }

  /**
   * Returns a set of members of this object. The set is ordered, and the order is in which the
   * elements were added.
   *
   * @return a set of members of this object.
   */
  public Set<Map.Entry<String, JsonElement>> entrySet() {
    return members.entrySet();
  }

  /**
   * Returns a set of members key values.
   *
   * @return a set of member keys as Strings
   * @since 2.8.1
   */
  public Set<String> keySet() {
    return members.keySet();
  }

  /**
   * Returns the number of key/value pairs in the object.
   *
   * @return the number of key/value pairs in the object.
   * @since 2.7
   */
  public int size() {
    return members.size();
  }

  /**
   * Returns true if the number of key/value pairs in the object is zero.
   *
   * @return true if the number of key/value pairs in the object is zero.
   * @since 2.10.1
   */
  public boolean isEmpty() {
    return members.isEmpty();
  }

  /**
   * Convenience method to check if a member with the specified name is present in this object.
   *
   * @param memberName name of the member that is being checked for presence.
   * @return true if there is a member with the specified name, false otherwise.
   */
  public boolean has(String memberName) {
    return members.containsKey(memberName);
  }

  /**
   * Returns the member with the specified name.
   *
   * @param memberName name of the member that is being requested.
   * @return the member matching the name, or {@code null} if no such member exists.
   */
  public JsonElement get(String memberName) {
    return members.get(memberName);
  }

  /**
   * Convenience method to get the specified member as a {@link JsonPrimitive}.
   *
   * @param memberName name of the member being requested.
   * @return the {@code JsonPrimitive} corresponding to the specified member, or {@code null} if no
   *     member with this name exists.
   * @throws ClassCastException if the member is not of type {@code JsonPrimitive}.
   */
  public JsonPrimitive getAsJsonPrimitive(String memberName) {
    return (JsonPrimitive) members.get(memberName);
  }

  /**
   * Convenience method to get the specified member as a {@link JsonArray}.
   *
   * @param memberName name of the member being requested.
   * @return the {@code JsonArray} corresponding to the specified member, or {@code null} if no
   *     member with this name exists.
   * @throws ClassCastException if the member is not of type {@code JsonArray}.
   */
  public JsonArray getAsJsonArray(String memberName) {
    return (JsonArray) members.get(memberName);
  }

  /**
   * Convenience method to get the specified member as a {@link JsonObject}.
   *
   * @param memberName name of the member being requested.
   * @return the {@code JsonObject} corresponding to the specified member, or {@code null} if no
   *     member with this name exists.
   * @throws ClassCastException if the member is not of type {@code JsonObject}.
   */
  public JsonObject getAsJsonObject(String memberName) {
    return (JsonObject) members.get(memberName);
  }

  /**
   * Returns a mutable {@link Map} view of this {@code JsonObject}. Changes to the {@code Map} are
   * visible in this {@code JsonObject} and the other way around.
   *
   * <p>The {@code Map} does not permit {@code null} keys or values. Unlike {@code JsonObject}'s
   * {@code null} handling, a {@link NullPointerException} is thrown when trying to add {@code
   * null}. Use {@link JsonNull} for JSON null values.
   *
   * @return mutable {@code Map} view
   * @since 2.10
   */
  public Map<String, JsonElement> asMap() {
    // It is safe to expose the underlying map because it disallows null keys and values
    return members;
  }

  /**
   * Returns whether the other object is equal to this. This method only considers the other object
   * to be equal if it is an instance of {@code JsonObject} and has equal members, ignoring order.
   */
  @Override
  public boolean equals(Object o) {
    return (o == this) || (o instanceof JsonObject && ((JsonObject) o).members.equals(members));
  }

  /**
   * Returns the hash code of this object. This method calculates the hash code based on the members
   * of this object, ignoring order.
   */
  @Override
  public int hashCode() {
    return members.hashCode();
  }
}
