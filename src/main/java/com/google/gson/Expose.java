package com.google.gson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that indicates this member should be exposed for 
 * Json serialization or deserialization.
 * This annotation has no effect unless you build {@link Gson} 
 * with a {@link GsonBuilder} and invoke 
 * {@link GsonBuilder#excludeFieldsWithoutExposeAnnotation()} method.
 *
 * @author Inderjeet Singh
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Expose {
  // This is a marker annotation with no additional properties 
}
