package com.google.gson.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

/**
 * An annotation which determines whether to serialize JSON {@code null}
 * property values or to omit the name-value property pair if the value is
 * a JSON {@code null}. This annotation overwrites the {@link Gson#serializeNulls()}
 * and {@link JsonWriter#getSerializeNulls()} setting but only affects the
 * direct value for the property. Nested {@code null}s, e.g. if the property
 * value is an object and itself contains a property with {@code null} value,
 * are not affected.
 *
 * <p>This annotation only takes effect after the type adapter for the
 * field has been applied. It is therefore possible that the type adapter
 * writes for a {@code null} field value a non-{@code null} JSON value in
 * which case this annotation has no effect.
 *
 * <p>The annotation prevents any changes to {@link JsonWriter#setSerializeNulls(boolean)}
 * done by the type adapter of the field from affecting the next direct
 * {@code null} value written to that writer. However these changes to that
 * setting are reflected by {@link JsonWriter#getSerializeNulls()} nonetheless
 * and affect any nested or subsequently written JSON value.
 *
 * <p>Here is an example showing how the annotation can be used:
 * <pre>
 * public class Person {
 *   private String favoriteColor;
 *
 *   &#64;SerializeNull // same as &#64;SerializeNull(true)
 *   private String favoriteFood;
 *
 *   &#64;SerializeNull(false)
 *   private String favoriteSportsTeam;
 * }
 * </pre>
 * By default {@link Gson} will not serialize JSON {@code null} property
 * values, so without the annotation {@code favoriteFood} would not
 * be present in the created JSON data in case its value is {@code null}.
 * However, because it is annotated the {@code null} value will be present
 * in the JSON data as well:
 * <pre>
 * Person person = new Person();
 * person.favoriteColor = null;
 * person.favoriteFood = null;
 * person.favoriteSportsTeam = null;
 *
 * String json = new Gson().toJson(person);
 * // {"favoriteFood":null}
 * </pre>
 * In case the Gson instance is created with {@link GsonBuilder#serializeNulls()}
 * it would always write {@code null} property values which might not always
 * be desired. Using {@code @SerializeNull(false)} overwrites this:
 * <pre>
 * Person person = new Person();
 * person.favoriteColor = null;
 * person.favoriteFood = null;
 * person.favoriteSportsTeam = null;
 *
 * String json = new GsonBuilder().serializeNulls().create().toJson(person);
 * // favoriteSportsTeam is not present due to &#64;SerializeNulls(false)
 * // {"favoriteColor":null,"favoriteFood":null}
 * </pre>
 *
 * @see GsonBuilder#serializeNulls()
 * @see JsonWriter#setSerializeNulls(boolean)
 * @see JsonWriter#forceNullValue()
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface SerializeNull {
  /**
   * Determines whether to serialize a {@code null} JSON property value.
   * {@code true} (the default) means that the property should be serialized.
   * {@code false} means that the property name-value pair should be omitted
   * if the value is {@code null}. However, a value of {@code false} has no
   * effect if the JSON {@code null} was {@linkplain JsonWriter#forceNullValue()
   * forcefully written}.
   *
   * @return whether to serialize a {@code null} JSON property value.
   */
  boolean value() default true;
}
