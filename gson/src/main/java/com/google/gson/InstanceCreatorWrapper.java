package com.google.gson;

import com.google.gson.stream.JsonReader;
import java.lang.reflect.Type;

public class InstanceCreatorWrapper<T> implements InstanceCreator<T> {

  @Override
  public T createInstance(Type type) {
    return null;
  }

  public T createInstance(Type type, JsonReader in) {
    return createInstance(type);
  }
}
