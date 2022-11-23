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

import com.google.gson.internal.NonNullElementWrapperList;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A class representing an array type in JSON. An array is a list of {@link JsonElement}s each of
 * which can be of a different type. This is an ordered list, meaning that the order in which
 * elements are added is preserved. This class does not support {@code null} values. If {@code null}
 * is provided as element argument to any of the methods, it is converted to a {@link JsonNull}.
 *
 * <p>{@code JsonArray} only implements the {@link Iterable} interface but not the {@link List}
 * interface. A {@code List} view of it can be obtained with {@link #asList()}.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public final class JsonArray extends JsonElement implements Iterable<JsonElement> {
  private final ArrayList<JsonElement> elements;

  /**
   * Creates an empty JsonArray.
   */
  @SuppressWarnings("deprecation") // superclass constructor
  public JsonArray() {
    elements = new ArrayList<>();
  }

  /**
   * Creates an empty JsonArray with the desired initial capacity.
   *
   * @param capacity initial capacity.
   * @throws IllegalArgumentException if the {@code capacity} is
   *   negative
   * @since 2.8.1
   */
  @SuppressWarnings("deprecation") // superclass constructor
  public JsonArray(int capacity) {
    elements = new ArrayList<>(capacity);
  }

  /**
   * Creates a deep copy of this element and all its children.
   *
   * @since 2.8.2
   */
  @Override
  public JsonArray deepCopy() {
    if (!elements.isEmpty()) {
      JsonArray result = new JsonArray(elements.size());
      for (JsonElement element : elements) {
        result.add(element.deepCopy());
      }
      return result;
    }
    return new JsonArray();
  }

  /**
   * Adds the specified boolean to self.
   *
   * @param bool the boolean that needs to be added to the array.
   * @since 2.4
   */
  public void add(Boolean bool) {
    elements.add(bool == null ? JsonNull.INSTANCE : new JsonPrimitive(bool));
  }

  /**
   * Adds the specified character to self.
   *
   * @param character the character that needs to be added to the array.
   * @since 2.4
   */
  public void add(Character character) {
    elements.add(character == null ? JsonNull.INSTANCE : new JsonPrimitive(character));
  }

  /**
   * Adds the specified number to self.
   *
   * @param number the number that needs to be added to the array.
   * @since 2.4
   */
  public void add(Number number) {
    elements.add(number == null ? JsonNull.INSTANCE : new JsonPrimitive(number));
  }

  /**
   * Adds the specified string to self.
   *
   * @param string the string that needs to be added to the array.
   * @since 2.4
   */
  public void add(String string) {
    elements.add(string == null ? JsonNull.INSTANCE : new JsonPrimitive(string));
  }

  /**
   * Adds the specified element to self.
   *
   * @param element the element that needs to be added to the array.
   */
  public void add(JsonElement element) {
    if (element == null) {
      element = JsonNull.INSTANCE;
    }
    elements.add(element);
  }

  /**
   * Adds all the elements of the specified array to self.
   *
   * @param array the array whose elements need to be added to the array.
   */
  public void addAll(JsonArray array) {
    elements.addAll(array.elements);
  }

  /**
   * Replaces the element at the specified position in this array with the specified element.
   *
   * @param index index of the element to replace
   * @param element element to be stored at the specified position
   * @return the element previously at the specified position
   * @throws IndexOutOfBoundsException if the specified index is outside the array bounds
   */
  public JsonElement set(int index, JsonElement element) {
    return elements.set(index, element == null ? JsonNull.INSTANCE : element);
  }

  /**
   * Removes the first occurrence of the specified element from this array, if it is present.
   * If the array does not contain the element, it is unchanged.
   *
   * @param element element to be removed from this array, if present
   * @return true if this array contained the specified element, false otherwise
   * @since 2.3
   */
  public boolean remove(JsonElement element) {
    return elements.remove(element);
  }

  /**
   * Removes the element at the specified position in this array. Shifts any subsequent elements
   * to the left (subtracts one from their indices). Returns the element that was removed from
   * the array.
   *
   * @param index index the index of the element to be removed
   * @return the element previously at the specified position
   * @throws IndexOutOfBoundsException if the specified index is outside the array bounds
   * @since 2.3
   */
  public JsonElement remove(int index) {
    return elements.remove(index);
  }

  /**
   * Returns true if this array contains the specified element.
   *
   * @return true if this array contains the specified element.
   * @param element whose presence in this array is to be tested
   * @since 2.3
   */
  public boolean contains(JsonElement element) {
    return elements.contains(element);
  }

  /**
   * Returns the number of elements in the array.
   *
   * @return the number of elements in the array.
   */
  public int size() {
    return elements.size();
  }

  /**
   * Returns true if the array is empty.
   *
   * @return true if the array is empty.
   * @since 2.8.7
   */
  public boolean isEmpty() {
    return elements.isEmpty();
  }

  /**
   * Returns an iterator to navigate the elements of the array. Since the array is an ordered list,
   * the iterator navigates the elements in the order they were inserted.
   *
   * @return an iterator to navigate the elements of the array.
   */
  @Override
  public Iterator<JsonElement> iterator() {
    return elements.iterator();
  }

  /**
   * Returns the i-th element of the array.
   *
   * @param i the index of the element that is being sought.
   * @return the element present at the i-th index.
   * @throws IndexOutOfBoundsException if i is negative or greater than or equal to the
   * {@link #size()} of the array.
   */
  public JsonElement get(int i) {
    return elements.get(i);
  }

  private JsonElement getAsSingleElement() {
    int size = elements.size();
    if (size == 1) {
      return elements.get(0);
    }
    throw new IllegalStateException("Array must have size 1, but has size " + size);
  }

  /**
   * Convenience method to get this array as a {@link Number} if it contains a single element.
   * This method calls {@link JsonElement#getAsNumber()} on the element, therefore any
   * of the exceptions declared by that method can occur.
   *
   * @return this element as a number if it is single element array.
   * @throws IllegalStateException if the array is empty or has more than one element.
   */
  @Override
  public Number getAsNumber() {
    return getAsSingleElement().getAsNumber();
  }

  /**
   * Convenience method to get this array as a {@link String} if it contains a single element.
   * This method calls {@link JsonElement#getAsString()} on the element, therefore any
   * of the exceptions declared by that method can occur.
   *
   * @return this element as a String if it is single element array.
   * @throws IllegalStateException if the array is empty or has more than one element.
   */
  @Override
  public String getAsString() {
    return getAsSingleElement().getAsString();
  }

  /**
   * Convenience method to get this array as a double if it contains a single element.
   * This method calls {@link JsonElement#getAsDouble()} on the element, therefore any
   * of the exceptions declared by that method can occur.
   *
   * @return this element as a double if it is single element array.
   * @throws IllegalStateException if the array is empty or has more than one element.
   */
  @Override
  public double getAsDouble() {
    return getAsSingleElement().getAsDouble();
  }

  /**
   * Convenience method to get this array as a {@link BigDecimal} if it contains a single element.
   * This method calls {@link JsonElement#getAsBigDecimal()} on the element, therefore any
   * of the exceptions declared by that method can occur.
   *
   * @return this element as a {@link BigDecimal} if it is single element array.
   * @throws IllegalStateException if the array is empty or has more than one element.
   * @since 1.2
   */
  @Override
  public BigDecimal getAsBigDecimal() {
    return getAsSingleElement().getAsBigDecimal();
  }

  /**
   * Convenience method to get this array as a {@link BigInteger} if it contains a single element.
   * This method calls {@link JsonElement#getAsBigInteger()} on the element, therefore any
   * of the exceptions declared by that method can occur.
   *
   * @return this element as a {@link BigInteger} if it is single element array.
   * @throws IllegalStateException if the array is empty or has more than one element.
   * @since 1.2
   */
  @Override
  public BigInteger getAsBigInteger() {
    return getAsSingleElement().getAsBigInteger();
  }

  /**
   * Convenience method to get this array as a float if it contains a single element.
   * This method calls {@link JsonElement#getAsFloat()} on the element, therefore any
   * of the exceptions declared by that method can occur.
   *
   * @return this element as a float if it is single element array.
   * @throws IllegalStateException if the array is empty or has more than one element.
   */
  @Override
  public float getAsFloat() {
    return getAsSingleElement().getAsFloat();
  }

  /**
   * Convenience method to get this array as a long if it contains a single element.
   * This method calls {@link JsonElement#getAsLong()} on the element, therefore any
   * of the exceptions declared by that method can occur.
   *
   * @return this element as a long if it is single element array.
   * @throws IllegalStateException if the array is empty or has more than one element.
   */
  @Override
  public long getAsLong() {
    return getAsSingleElement().getAsLong();
  }

  /**
   * Convenience method to get this array as an integer if it contains a single element.
   * This method calls {@link JsonElement#getAsInt()} on the element, therefore any
   * of the exceptions declared by that method can occur.
   *
   * @return this element as an integer if it is single element array.
   * @throws IllegalStateException if the array is empty or has more than one element.
   */
  @Override
  public int getAsInt() {
    return getAsSingleElement().getAsInt();
  }

  /**
   * Convenience method to get this array as a primitive byte if it contains a single element.
   * This method calls {@link JsonElement#getAsByte()} on the element, therefore any
   * of the exceptions declared by that method can occur.
   *
   * @return this element as a primitive byte if it is single element array.
   * @throws IllegalStateException if the array is empty or has more than one element.
   */
  @Override
  public byte getAsByte() {
    return getAsSingleElement().getAsByte();
  }

  /**
   * Convenience method to get this array as a character if it contains a single element.
   * This method calls {@link JsonElement#getAsCharacter()} on the element, therefore any
   * of the exceptions declared by that method can occur.
   *
   * @return this element as a primitive short if it is single element array.
   * @throws IllegalStateException if the array is empty or has more than one element.
   * @deprecated This method is misleading, as it does not get this element as a char but rather as
   * a string's first character.
   */
  @Deprecated
  @Override
  public char getAsCharacter() {
    return getAsSingleElement().getAsCharacter();
  }

  /**
   * Convenience method to get this array as a primitive short if it contains a single element.
   * This method calls {@link JsonElement#getAsShort()} on the element, therefore any
   * of the exceptions declared by that method can occur.
   *
   * @return this element as a primitive short if it is single element array.
   * @throws IllegalStateException if the array is empty or has more than one element.
   */
  @Override
  public short getAsShort() {
    return getAsSingleElement().getAsShort();
  }

  /**
   * Convenience method to get this array as a boolean if it contains a single element.
   * This method calls {@link JsonElement#getAsBoolean()} on the element, therefore any
   * of the exceptions declared by that method can occur.
   *
   * @return this element as a boolean if it is single element array.
   * @throws IllegalStateException if the array is empty or has more than one element.
   */
  @Override
  public boolean getAsBoolean() {
    return getAsSingleElement().getAsBoolean();
  }

  /**
   * Returns a mutable {@link List} view of this {@code JsonArray}. Changes to the {@code List}
   * are visible in this {@code JsonArray} and the other way around.
   *
   * <p>The {@code List} does not permit {@code null} elements. Unlike {@code JsonArray}'s
   * {@code null} handling, a {@link NullPointerException} is thrown when trying to add {@code null}.
   * Use {@link JsonNull} for JSON null values.
   *
   * @return mutable {@code List} view
   * @since 2.10
   */
  public List<JsonElement> asList() {
    return new NonNullElementWrapperList<>(elements);
  }

  /**
   * Returns whether the other object is equal to this. This method only considers
   * the other object to be equal if it is an instance of {@code JsonArray} and has
   * equal elements in the same order.
   */
  @Override
  public boolean equals(Object o) {
    return (o == this) || (o instanceof JsonArray && ((JsonArray) o).elements.equals(elements));
  }

  /**
   * Returns the hash code of this array. This method calculates the hash code based
   * on the elements of this array.
   */
  @Override
  public int hashCode() {
    return elements.hashCode();
  }
}
