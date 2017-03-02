package com.google.gson;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import com.google.gson.annotations.JsonAdapter;

/**
 * Factory for producing {@link TypeAdapter} that only read / write specific fields
 * with some given annotation.
 * <p>
 * Configured through {@link GsonBuilder#registerFieldAdapterFactory}.
 * <p>
 * Normally a field needs only one adapter, so it doesn't make sense if both this
 * and {@link JsonAdapter} are applied with a single field. In that case, this will
 * be ignored.
 *
 * @author Floyd Wan
 * @since 2.8.1
 */
public interface FieldAdapterFactory {

  /**
   * Create a {@link TypeAdapter} for a field with a special annotation.
   *
   * @param annotation the annotation instance on the given {@code field}
   * @param field the field needs to be investigated
   * @param <T> the target type of {@link TypeAdapter}
   * @return a {@link TypeAdapter} to read / write this very field.
   */
  <T> TypeAdapter<T> create(Annotation annotation, Field field);
}
