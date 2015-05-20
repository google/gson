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

import com.google.gson.internal.LinkedTreeMap;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.Set;

/**
 * A class representing an object type in Json. An object consists of name-value pairs where names
 * are strings, and values are any other type of {@link JsonElement}. This allows for a creating a
 * tree of JsonElements. The member elements of this object are maintained in order they were added.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public final class JsonObject extends JsonElement {
  private final LinkedTreeMap<String, JsonElement> members =
      new LinkedTreeMap<String, JsonElement>();

  @Override
  JsonObject deepCopy() {
    JsonObject result = new JsonObject();
    for (Map.Entry<String, JsonElement> entry : members.entrySet()) {
      result.add(entry.getKey(), entry.getValue().deepCopy());
    }
    return result;
  }

  /**
   * Adds a member, which is a name-value pair, to self. The name must be a String, but the value
   * can be an arbitrary JsonElement, thereby allowing you to build a full tree of JsonElements
   * rooted at this node.
   *
   * @param property name of the member.
   * @param value the member object.
   */
  public void add(String property, JsonElement value) {
    if (value == null) {
      value = JsonNull.INSTANCE;
    }
    members.put(property, value);
  }

  /**
   * Removes the {@code property} from this {@link JsonObject}.
   *
   * @param property name of the member that should be removed.
   * @return the {@link JsonElement} object that is being removed.
   * @since 1.3
   */
  public JsonElement remove(String property) {
    return members.remove(property);
  }

  /**
   * Convenience method to add a primitive member. The specified value is converted to a
   * JsonPrimitive of String.
   *
   * @param property name of the member.
   * @param value the string value associated with the member.
   */
  public void addProperty(String property, String value) {
    add(property, createJsonElement(value));
  }

  /**
   * Convenience method to add a primitive member. The specified value is converted to a
   * JsonPrimitive of Number.
   *
   * @param property name of the member.
   * @param value the number value associated with the member.
   */
  public void addProperty(String property, Number value) {
    add(property, createJsonElement(value));
  }

  /**
   * Convenience method to add a boolean member. The specified value is converted to a
   * JsonPrimitive of Boolean.
   *
   * @param property name of the member.
   * @param value the number value associated with the member.
   */
  public void addProperty(String property, Boolean value) {
    add(property, createJsonElement(value));
  }

  /**
   * Convenience method to add a char member. The specified value is converted to a
   * JsonPrimitive of Character.
   *
   * @param property name of the member.
   * @param value the number value associated with the member.
   */
  public void addProperty(String property, Character value) {
    add(property, createJsonElement(value));
  }

  /**
   * Creates the proper {@link JsonElement} object from the given {@code value} object.
   *
   * @param value the object to generate the {@link JsonElement} for
   * @return a {@link JsonPrimitive} if the {@code value} is not null, otherwise a {@link JsonNull}
   */
  private JsonElement createJsonElement(Object value) {
    return value == null ? JsonNull.INSTANCE : new JsonPrimitive(value);
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
   * @return the member matching the name. Null if no such member exists.
   */
  public JsonElement get(String memberName) {
    return members.get(memberName);
  }

  /**
   * Returns the member with the specified name if it exists, otherwise the supplied default value
   * is returned.
   *
   * @param memberName name of the member that is being requested.
   * @param defaultValue the value to be returned if the member does not exist.
   * @return the member matching the name. <code>defaultValue</code> if no such member exists.
   */
  public JsonElement get(String memberName, JsonElement defaultValue) {
    JsonElement result = get(memberName);
    return result != null ? result : defaultValue;
  }

  /**
   * Convenience method to get the specified member as a JsonPrimitive element.
   *
   * @param memberName name of the member being requested.
   * @return the JsonPrimitive corresponding to the specified member.
   */
  public JsonPrimitive getAsJsonPrimitive(String memberName) {
    return (JsonPrimitive) get(memberName);
  }

  /**
   * Convenience method to get the specified member as a JsonPrimitive element if it exists,
   * otherwise the supplied default value is returned.
   *
   * @param memberName name of the member being requested.
   * @param defaultValue the value to be returned if the member does not exist.
   * @return the JsonPrimitive corresponding to the specified member. <code>defaultValue</code>
   * if no such member exists.
   */
  public JsonPrimitive getAsJsonPrimitive(String memberName, JsonPrimitive defaultValue) {
    JsonPrimitive result = getAsJsonPrimitive(memberName);
    return result != null ? result : defaultValue;
  }

  /**
   * Convenience method to get the specified member as a JsonArray.
   *
   * @param memberName name of the member being requested.
   * @return the JsonArray corresponding to the specified member.
   */
  public JsonArray getAsJsonArray(String memberName) {
    return (JsonArray) get(memberName);
  }

  /**
   * Convenience method to get the specified member as a JsonArray if it exists, otherwise the
   * supplied default value is returned.
   *
   * @param memberName name of the member being requested.
   * @param defaultValue the value to be returned if the member does not exist.
   * @return the JsonArray corresponding to the specified member. <code>defaultValue</code> if no
   * such member exists.
   */
  public JsonArray getAsJsonArray(String memberName, JsonArray defaultValue) {
    JsonArray result = getAsJsonArray(memberName);
    return result != null ? result : defaultValue;
  }

  /**
   * Convenience method to get the specified member as a JsonObject.
   *
   * @param memberName name of the member being requested.
   * @return the JsonObject corresponding to the specified member.
   */
  public JsonObject getAsJsonObject(String memberName) {
    return (JsonObject) get(memberName);
  }

  /**
   * Convenience method to get the specified member as a JsonObject if it exists, otherwise the
   * supplied default value is returned.
   *
   * @param memberName name of the member being requested.
   * @param defaultValue the value to be returned if the member does not exist.
   * @return the JsonObject corresponding to the specified member. <code>defaultValue</code> if no
   * such member exists.
   */
  public JsonObject getAsJsonObject(String memberName, JsonObject defaultValue) {
    JsonObject result = getAsJsonObject(memberName);
    return result != null ? result : defaultValue;
  }

  /**
   * Convenience method to get the specified member as a boolean value.
   *
   * @param memberName name of the member being requested.
   * @return the boolean corresponding to the specified member.
   * @throws ClassCastException if the member is not a {@link JsonPrimitive} and is not a valid
   * boolean value.
   * @throws IllegalStateException if the member is a {@link JsonArray} but contains
   * more than a single element.
   * @throws NullPointerException if the member does not exist.
   */
  public boolean getAsBoolean(String memberName) {
    try {
      return get(memberName).getAsBoolean();
    } catch (UnsupportedOperationException e) {
      throw new ClassCastException("Member is not a value type");
    }
  }

  /**
   * Convenience method to get the specified member as a boolean value if it exists, otherwise the
   * supplied default value is returned.
   *
   * @param memberName name of the member being requested.
   * @param defaultValue the value to be returned if the member does not exist.
   * @return the boolean corresponding to the specified member. <code>defaultValue</code> if no
   * such member exists.
   * @throws ClassCastException if the member is not a {@link JsonPrimitive} and is not a valid
   * boolean value.
   * @throws IllegalStateException if the member is a {@link JsonArray} but contains
   * more than a single element.
   */
  public boolean getAsBoolean(String memberName, boolean defaultValue) {
    try {
      return getAsBoolean(memberName);
    } catch (NullPointerException e) {
      return defaultValue;
    }
  }

  /**
   * Convenience method to get the specified member as a {@link Boolean} value.
   *
   * @param memberName name of the member being requested.
   * @return the {@link Boolean} corresponding to the specified member.
   * @throws ClassCastException if the member is not a {@link JsonPrimitive} and is not a valid
   * {@link Boolean} value.
   * @throws IllegalStateException if the member is a {@link JsonArray} but contains
   * more than a single element.
   * @throws NullPointerException if the member does not exist.
   */
  Boolean getAsBooleanWrapper(String memberName) {
    try {
      return get(memberName).getAsBooleanWrapper();
    } catch (UnsupportedOperationException e) {
      throw new ClassCastException("Member is not a value type");
    }
  }

  /**
   * Convenience method to get the specified member as a {@link Boolean} value if it exists,
   * otherwise the supplied default value is returned.
   *
   * @param memberName name of the member being requested.
   * @param defaultValue the value to be returned if the member does not exist.
   * @return the {@link Boolean} corresponding to the specified member. <code>defaultValue</code>
   * if no such member exists.
   * @throws ClassCastException if the member is not a {@link JsonPrimitive} and is not a valid
   * {@link Boolean} value.
   * @throws IllegalStateException if the member is a {@link JsonArray} but contains
   * more than a single element.
   */
  public Boolean getAsBooleanWrapper(String memberName, Boolean defaultValue) {
    try {
      return getAsBooleanWrapper(memberName);
    } catch (NullPointerException e) {
      return defaultValue;
    }
  }

  /**
   * Convenience method to get the specified member as a {@link Number}.
   *
   * @param memberName name of the member being requested.
   * @return the {@link Number} corresponding to the specified member.
   * @throws ClassCastException if the member is not a {@link JsonPrimitive} and is not a valid
   * {@link Number} value.
   * @throws IllegalStateException if the member is a {@link JsonArray} but contains
   * more than a single element.
   * @throws NullPointerException if the member does not exist.
   */
  public Number getAsNumber(String memberName) {
    try {
      return get(memberName).getAsNumber();
    } catch (UnsupportedOperationException e) {
      throw new ClassCastException("Member is not a value type");
    }
  }

  /**
   * Convenience method to get the specified member as a {@link Number} value if it exists,
   * otherwise the supplied default value is returned.
   *
   * @param memberName name of the member being requested.
   * @param defaultValue the value to be returned if the member does not exist.
   * @return the {@link Number} corresponding to the specified member. <code>defaultValue</code>
   * if no such member exists.
   * @throws ClassCastException if the member is not a {@link JsonPrimitive} and is not a valid
   * {@link Number} value.
   * @throws IllegalStateException if the member is a {@link JsonArray} but contains
   * more than a single element.
   */
  public Number getAsNumber(String memberName, Number defaultValue) {
    try {
      return getAsNumber(memberName);
    } catch (NullPointerException e) {
      return defaultValue;
    }
  }

  /**
   * Convenience method to get the specified member as a {@link String} value.
   *
   * @param memberName name of the member being requested.
   * @return the {@link String} corresponding to the specified member.
   * @throws ClassCastException if the member is not a {@link JsonPrimitive} and is not a valid
   * {@link String} value.
   * @throws IllegalStateException if the member is a {@link JsonArray} but contains
   * more than a single element.
   * @throws NullPointerException if the member does not exist.
   */
  public String getAsString(String memberName) {
    try {
      return get(memberName).getAsString();
    } catch (UnsupportedOperationException e) {
      throw new ClassCastException("Member is not a value type");
    }
  }

  /**
   * Convenience method to get the specified member as a {@link String} value if it exists,
   * otherwise the supplied default value is returned.
   *
   * @param memberName name of the member being requested.
   * @param defaultValue the value to be returned if the member does not exist.
   * @return the {@link String} corresponding to the specified member. <code>defaultValue</code>
   * if no such member exists.
   * @throws ClassCastException if the member is not a {@link JsonPrimitive} and is not a valid
   * {@link String} value.
   * @throws IllegalStateException if the member is a {@link JsonArray} but contains
   * more than a single element.
   */
  public String getAsString(String memberName, String defaultValue) {
    try {
      return getAsString(memberName);
    } catch (NullPointerException e) {
      return defaultValue;
    }
  }

  /**
   * Convenience method to get the specified member as a double value.
   *
   * @param memberName name of the member being requested.
   * @return the double corresponding to the specified member.
   * @throws ClassCastException if the member is not a {@link JsonPrimitive} and is not a valid
   * double value.
   * @throws IllegalStateException if the member is a {@link JsonArray} but contains
   * more than a single element.
   * @throws NullPointerException if the member does not exist.
   */
  public double getAsDouble(String memberName) {
    try {
      return get(memberName).getAsDouble();
    } catch (UnsupportedOperationException e) {
      throw new ClassCastException("Member is not a value type");
    }
  }

  /**
   * Convenience method to get the specified member as a double value if it exists, otherwise the
   * supplied default value is returned.
   *
   * @param memberName name of the member being requested.
   * @param defaultValue the value to be returned if the member does not exist.
   * @return the double corresponding to the specified member. <code>defaultValue</code> if no
   * such member exists.
   * @throws ClassCastException if the member is not a {@link JsonPrimitive} and is not a valid
   * double value.
   * @throws IllegalStateException if the member is a {@link JsonArray} but contains
   * more than a single element.
   */
  public double getAsDouble(String memberName, double defaultValue) {
    try {
      return getAsDouble(memberName);
    } catch (NullPointerException e) {
      return defaultValue;
    }
  }

  /**
   * Convenience method to get the specified member as a float value.
   *
   * @param memberName name of the member being requested.
   * @return the float corresponding to the specified member.
   * @throws ClassCastException if the member is not a {@link JsonPrimitive} and is not a valid
   * float value.
   * @throws IllegalStateException if the member is a {@link JsonArray} but contains
   * more than a single element.
   * @throws NullPointerException if the member does not exist.
   */
  public float getAsFloat(String memberName) {
    try {
      return get(memberName).getAsFloat();
    } catch (UnsupportedOperationException e) {
      throw new ClassCastException("Member is not a value type");
    }
  }

  /**
   * Convenience method to get the specified member as a float value if it exists, otherwise the
   * supplied default value is returned.
   *
   * @param memberName name of the member being requested.
   * @param defaultValue the value to be returned if the member does not exist.
   * @return the float corresponding to the specified member. <code>defaultValue</code> if no
   * such member exists.
   * @throws ClassCastException if the member is not a {@link JsonPrimitive} and is not a valid
   * float value.
   * @throws IllegalStateException if the member is a {@link JsonArray} but contains
   * more than a single element.
   */
  public float getAsFloat(String memberName, float defaultValue) {
    try {
      return getAsFloat(memberName);
    } catch (NullPointerException e) {
      return defaultValue;
    }
  }

  /**
   * Convenience method to get the specified member as a long value.
   *
   * @param memberName name of the member being requested.
   * @return the long corresponding to the specified member.
   * @throws ClassCastException if the member is not a {@link JsonPrimitive} and is not a valid
   * long value.
   * @throws IllegalStateException if the member is a {@link JsonArray} but contains
   * more than a single element.
   * @throws NullPointerException if the member does not exist.
   */
  public long getAsLong(String memberName) {
    try {
      return get(memberName).getAsLong();
    } catch (UnsupportedOperationException e) {
      throw new ClassCastException("Member is not a value type");
    }
  }

  /**
   * Convenience method to get the specified member as a long value if it exists, otherwise the
   * supplied default value is returned.
   *
   * @param memberName name of the member being requested.
   * @param defaultValue the value to be returned if the member does not exist.
   * @return the long corresponding to the specified member. <code>defaultValue</code> if no
   * such member exists.
   * @throws ClassCastException if the member is not a {@link JsonPrimitive} and is not a valid
   * long value.
   * @throws IllegalStateException if the member is a {@link JsonArray} but contains
   * more than a single element.
   */
  public long getAsLong(String memberName, long defaultValue) {
    try {
      return getAsLong(memberName);
    } catch (NullPointerException e) {
      return defaultValue;
    }
  }

  /**
   * Convenience method to get the specified member as a integer value.
   *
   * @param memberName name of the member being requested.
   * @return the int corresponding to the specified member.
   * @throws ClassCastException if the member is not a {@link JsonPrimitive} and is not a valid
   * int value.
   * @throws IllegalStateException if the member is a {@link JsonArray} but contains
   * more than a single element.
   * @throws NullPointerException if the member does not exist.
   */
  public int getAsInt(String memberName) {
    try {
      return get(memberName).getAsInt();
    } catch (UnsupportedOperationException e) {
      throw new ClassCastException("Member is not a value type");
    }
  }

  /**
   * Convenience method to get the specified member as a int value if it exists, otherwise the
   * supplied default value is returned.
   *
   * @param memberName name of the member being requested.
   * @param defaultValue the value to be returned if the member does not exist.
   * @return the int corresponding to the specified member. <code>defaultValue</code> if no
   * such member exists.
   * @throws ClassCastException if the member is not a {@link JsonPrimitive} and is not a valid
   * int value.
   * @throws IllegalStateException if the member is a {@link JsonArray} but contains
   * more than a single element.
   */
  public int getAsInt(String memberName, int defaultValue) {
    try {
      return getAsInt(memberName);
    } catch (NullPointerException e) {
      return defaultValue;
    }
  }

  /**
   * Convenience method to get the specified member as a short value.
   *
   * @param memberName name of the member being requested.
   * @return the short corresponding to the specified member.
   * @throws ClassCastException if the member is not a {@link JsonPrimitive} and is not a valid
   * short value.
   * @throws IllegalStateException if the member is a {@link JsonArray} but contains
   * more than a single element.
   * @throws NullPointerException if the member does not exist.
   */
  public short getAsShort(String memberName) {
    try {
      return get(memberName).getAsShort();
    } catch (UnsupportedOperationException e) {
      throw new ClassCastException("Member is not a value type");
    }
  }

  /**
   * Convenience method to get the specified member as a short value if it exists, otherwise the
   * supplied default value is returned.
   *
   * @param memberName name of the member being requested.
   * @param defaultValue the value to be returned if the member does not exist.
   * @return the short corresponding to the specified member. <code>defaultValue</code> if no
   * such member exists.
   * @throws ClassCastException if the member is not a {@link JsonPrimitive} and is not a valid
   * short value.
   * @throws IllegalStateException if the member is a {@link JsonArray} but contains
   * more than a single element.
   */
  public short getAsShort(String memberName, short defaultValue) {
    try {
      return getAsShort(memberName);
    } catch (NullPointerException e) {
      return defaultValue;
    }
  }

  /**
   * Convenience method to get the specified member as a byte value.
   *
   * @param memberName name of the member being requested.
   * @return the byte corresponding to the specified member.
   * @throws ClassCastException if the member is not a {@link JsonPrimitive} and is not a valid
   * byte value.
   * @throws IllegalStateException if the member is a {@link JsonArray} but contains
   * more than a single element.
   * @throws NullPointerException if the member does not exist.
   */
  public byte getAsByte(String memberName) {
    try {
      return get(memberName).getAsByte();
    } catch (UnsupportedOperationException e) {
      throw new ClassCastException("Member is not a value type");
    }
  }

  /**
   * Convenience method to get the specified member as a byte value if it exists, otherwise the
   * supplied default value is returned.
   *
   * @param memberName name of the member being requested.
   * @param defaultValue the value to be returned if the member does not exist.
   * @return the byte corresponding to the specified member. <code>defaultValue</code> if no
   * such member exists.
   * @throws ClassCastException if the member is not a {@link JsonPrimitive} and is not a valid
   * byte value.
   * @throws IllegalStateException if the member is a {@link JsonArray} but contains
   * more than a single element.
   */
  public byte getAsByte(String memberName, byte defaultValue) {
    try {
      return getAsByte(memberName);
    } catch (NullPointerException e) {
      return defaultValue;
    }
  }

  /**
   * Convenience method to get the specified member as a character value.
   *
   * @param memberName name of the member being requested.
   * @return the char corresponding to the specified member.
   * @throws ClassCastException if the member is not a {@link JsonPrimitive} and is not a valid
   * char value.
   * @throws IllegalStateException if the member is a {@link JsonArray} but contains
   * more than a single element.
   * @throws NullPointerException if the member does not exist.
   */
  public char getAsCharacter(String memberName) {
    try {
      return get(memberName).getAsCharacter();
    } catch (UnsupportedOperationException e) {
      throw new ClassCastException("Member is not a value type");
    }
  }

  /**
   * Convenience method to get the specified member as a char value if it exists, otherwise the
   * supplied default value is returned.
   *
   * @param memberName name of the member being requested.
   * @param defaultValue the value to be returned if the member does not exist.
   * @return the char corresponding to the specified member. <code>defaultValue</code> if no
   * such member exists.
   * @throws ClassCastException if the member is not a {@link JsonPrimitive} and is not a valid
   * char value.
   * @throws IllegalStateException if the member is a {@link JsonArray} but contains
   * more than a single element.
   */
  public char getAsCharacter(String memberName, char defaultValue) {
    try {
      return getAsCharacter(memberName);
    } catch (NullPointerException e) {
      return defaultValue;
    }
  }

  /**
   * Convenience method to get the specified member as a {@link BigDecimal}.
   *
   * @param memberName name of the member being requested.
   * @return the {@link BigDecimal} corresponding to the specified member.
   * @throws ClassCastException if the member is not a {@link JsonPrimitive} and is not a valid
   * {@link BigDecimal} value.
   * @throws IllegalStateException if the member is a {@link JsonArray} but contains
   * more than a single element.
   * @throws NullPointerException if the member does not exist.
   */
  public BigDecimal getAsBigDecimal(String memberName) {
    try {
      return get(memberName).getAsBigDecimal();
    } catch (UnsupportedOperationException e) {
      throw new ClassCastException("Member is not a value type");
    }
  }

  /**
   * Convenience method to get the specified member as a {@link BigDecimal} value if it exists,
   * otherwise the supplied default value is returned.
   *
   * @param memberName name of the member being requested.
   * @param defaultValue the value to be returned if the member does not exist.
   * @return the {@link BigDecimal} corresponding to the specified member. <code>defaultValue</code>
   * if no such member exists.
   * @throws ClassCastException if the member is not a {@link JsonPrimitive} and is not a valid
   * {@link BigDecimal} value.
   * @throws IllegalStateException if the member is a {@link JsonArray} but contains
   * more than a single element.
   */
  public BigDecimal getAsBigDecimal(String memberName, BigDecimal defaultValue) {
    try {
      return getAsBigDecimal(memberName);
    } catch (NullPointerException e) {
      return defaultValue;
    }
  }

  /**
   * Convenience method to get the specified member as a {@link BigInteger}.
   *
   * @param memberName name of the member being requested.
   * @return the {@link BigInteger} corresponding to the specified member.
   * @throws ClassCastException if the member is not a {@link JsonPrimitive} and is not a valid
   * {@link BigInteger} value.
   * @throws IllegalStateException if the member is a {@link JsonArray} but contains
   * more than a single element.
   * @throws NullPointerException if the member does not exist.
   */
  public BigInteger getAsBigInteger(String memberName) {
    try {
      return get(memberName).getAsBigInteger();
    } catch (UnsupportedOperationException e) {
      throw new ClassCastException("Member is not a value type");
    }
  }

  /**
   * Convenience method to get the specified member as a {@link BigInteger} value if it exists,
   * otherwise the supplied default value is returned.
   *
   * @param memberName name of the member being requested.
   * @param defaultValue the value to be returned if the member does not exist.
   * @return the {@link BigInteger} corresponding to the specified member. <code>defaultValue</code>
   * if no such member exists.
   * @throws ClassCastException if the member is not a {@link JsonPrimitive} and is not a valid
   * {@link BigInteger} value.
   * @throws IllegalStateException if the member is a {@link JsonArray} but contains
   * more than a single element.
   */
  public BigInteger getAsBigInteger(String memberName, BigInteger defaultValue) {
    try {
      return getAsBigInteger(memberName);
    } catch (NullPointerException e) {
      return defaultValue;
    }
  }

  @Override
  public boolean equals(Object o) {
    return (o == this) || (o instanceof JsonObject
        && ((JsonObject) o).members.equals(members));
  }

  @Override
  public int hashCode() {
    return members.hashCode();
  }
}
