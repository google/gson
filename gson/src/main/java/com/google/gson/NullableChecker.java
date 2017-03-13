package com.google.gson;

import java.lang.reflect.Field;

/**
 * This interface is used to check if a field supports null values.
 *
 * The implementor is expected to search for custom annotations in fields.
 * For instance, the following example uses the
 * {@code javax.validation.constraints.NotNull annotation}:
 *
 * <pre>{@code
 * class AnnotationNullableChecker implements NullableChecker {
 *   public boolean fieldIsNullable(Field field) {
 *     return field.getAnnotation(javax.validation.constraints.NotNull.class)
 *       != null;
 *   }
 * }
 * }</pre>
 *
 * @author Juan Luis Boya García
 */
public interface NullableChecker {
  boolean fieldIsNullable(Field field);
}