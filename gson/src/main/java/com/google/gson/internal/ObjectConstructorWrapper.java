package com.google.gson.internal;

import com.google.gson.stream.JsonReader;

public class ObjectConstructorWrapper<T> implements ObjectConstructor<T> {

  @Override
  public T construct() {
    return null;
  }

  public T construct(JsonReader in) {
    return construct();
  }
}
