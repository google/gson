package com.google.gson;

import java.lang.reflect.Field;

/**
 * Excludes fields that do not have the {@link Expose} annotation
 * 
 * @author Inderjeet Singh
 */
class ExposeAnnotationBasedExclusionStrategy implements ExclusionStrategy {

  public boolean shouldSkipClass(Class<?> clazz) {
    return false;
  }

  public boolean shouldSkipField(Field f) {
    return f.getAnnotation(Expose.class) == null;
  }
}
