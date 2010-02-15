package com.google.gson.webservice.definition;

public class TypedKey<T> {
  private final String name;
  private final Class<T> classOfT;

  public TypedKey(String name, Class<T> classOfT) {
    this.name = name;
    this.classOfT = classOfT;
  }

  public String getName() {
    return name;
  }

  public Class<T> getClassOfT() {
    return classOfT;
  }
}
