package com.google.gson.internal;

import com.google.gson.stream.JsonReader;

/**
 * Acts as a wrapper for instances of the {@link ObjectConstructor} interface for fill-in. In order
 * to maintain backwards compatability, implements the {@code ObjectConstructor} interface. That way
 * when the {@link ConstructorConstructor} returns an {@code ObjectConstructor}, it's actually
 * wrapped in this class and castable to this class. This doesn't change the functionality for
 * existing code (since it just treats them as implementers of {@code ObjectConstructor}) but allows
 * the creation of objects to depend on the passed in {@link JsonReader} object.
 */
public class ObjectConstructorWrapper<T> implements ObjectConstructor<T> {

  @Override
  public T construct() {
    return null;
  }

  /**
   * If not defined, returns the result of the {@link #construct} method. This method is designed to
   * create an object of class T to suport the Fill-In mechanic {@link
   * GsonBuilder#registerTypeAdapterFactory}.
   *
   * @param in is the JsonReader from which the object should be constructed.
   * @return returns the object of class T that was constructed based on the {@code in} stream.
   */
  public T construct(JsonReader in) {
    return construct();
  }
}
