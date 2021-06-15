package com.google.gson;

import com.google.gson.stream.JsonReader;
import java.lang.reflect.Type;

/**
 * Acts as a wrapper of the {@link InstanceCreator} interface for fill-in. Fulfills roughly the same
 * functionality as the {@link com.google.gson.internal.ObjectConstructorWrapper} class. It doesn't
 * break existing functionality by implementing {@code InstanceCreator} but when casted to this
 * class, it can expose a method that accepts a {@code JsonReader} as a parameter, allowing fill-in
 * functionality.
 */
public class InstanceCreatorWrapper<T> implements InstanceCreator<T> {

  @Override
  public T createInstance(Type type) {
    return null;
  }

  /**
   * If not defined, defaults to returning the result of {@link #createInstance}. This method is
   * designed to help with creating Adapters with Fill-In. See {@link
   * GsonBuilder#registerTypeAdapterFactory}.
   *
   * @param type the parameterized T represented as a {@link Type}.
   * @param in the JsonReader from which to create the instance.
   * @return a default object instance of type T.
   */
  public T createInstance(Type type, JsonReader in) {
    return createInstance(type);
  }
}
