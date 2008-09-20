package com.google.gson.wsf;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

class ParamMap {

  public static class Builder<T extends ParamMapSpec> {    
    protected final Map<String, Object> contents = Maps.newLinkedHashMap();
    protected final T spec;

    public Builder(T spec) {
      this.spec = spec;
    }

    /**
     * If value is a generic type, use {@link #put(String, Object, Type)} instead.
     * 
     * @param key
     * @param value
     */
    public Builder<T> put(String paramName, Object content) {
      return put(paramName, content, content.getClass());
    }

    public Builder<T> put(String paramName, Object content, Type typeOfContent) {
      spec.checkIfCompatible(paramName, typeOfContent);
      contents.put(paramName, content);
      return this;
    }
  }
  
  protected final Map<String, Object> contents;
  protected final ParamMapSpec spec;

  protected ParamMap(ParamMapSpec spec, Map<String, Object> contents) {
    this.spec = spec;
    this.contents = contents;
  }

  public ParamMapSpec getSpec() {
    return spec;
  }

  public Object get(String paramName) {
    return contents.get(paramName);
  }

  @SuppressWarnings("unchecked")
  public <T> T get(String key, Type typeOfValue) {
    Preconditions.checkArgument(spec.checkIfCompatible(key, typeOfValue));
    return (T) contents.get(key);
  }
  
  public Type getSpec(String headerName) {
    return spec.getTypeFor(headerName);
  }

  public Set<Map.Entry<String, Object>> entrySet() {
    return contents.entrySet();
  }

  public int size() {
    return contents.size();
  }

  @Override
  public String toString() {
    return Util.toStringMap(contents);
  }
}
