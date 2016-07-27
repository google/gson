package com.google.gson.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation stating that a field is to be omitted from serialization,
 * if its value equals to the default one (taken from a newly constructed object.
 * <p>
 * Setting {@code value} to {@code false} switches this behavior off.
 * Placing this annotation on a class affects all its fields.
 * An annotation placed on a field takes precedence over one placed on its class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface OmitDefaultValue {
  boolean value() default true;
}
