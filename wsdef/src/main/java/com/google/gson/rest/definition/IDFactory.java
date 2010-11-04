package com.google.gson.rest.definition;

import java.lang.reflect.Type;

public class IDFactory<I extends ID> {
  private final Class<? super I> classOfI;
  private final Type typeOfId;

  public IDFactory(Class<? super I> classOfI, Type typeOfId) {
    this.classOfI = classOfI;
    this.typeOfId = typeOfId;
  }

  public I createId(long value) {
    if (classOfI.isAssignableFrom(Id.class)) {
      return (I)Id.get(value, typeOfId);
    } 
    throw new UnsupportedOperationException();
  }
}
