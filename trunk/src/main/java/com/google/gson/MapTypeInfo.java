package com.google.gson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

final class MapTypeInfo {
  
  private final ParameterizedType mapType;

  public MapTypeInfo(Type mapType) {
    this.mapType = (ParameterizedType) mapType;
  }
  
  public Type getKeyType() {    
    return mapType.getActualTypeArguments()[0];
  }
  
  public Type getValueType() {    
    return mapType.getActualTypeArguments()[1];
  }
}
