package com.google.gson.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;

/**
 * An annotation which forces that fields whose value are {@code null} are
 * serialized with a JSON {@code null} value regardless of the
 * {@link JsonWriter#getSerializeNulls()} setting. The {@link #checkTime()}
 * element defines when to perform the {@code null} check.
 *
 * <p>Here is an example showing how the annotation can be used:
 * <pre>
 * public class Person {
 *   private String name;
 *   &#64;ForceSerializeNull
 *   private String favoriteFood;
 * }
 * </pre>
 * By default {@link Gson} will not serialize fields whose value is
 * {@code null}, so without the annotation {@code favoriteFood} would not
 * be present in the created JSON data in case its value is {@code null}.
 * However, because it is annotated the {@code null} value will be present
 * in the JSON data as well:
 * <pre>
 * Person person = new Person();
 * person.name = "John Doe";
 * person.favoriteFood = null;
 *
 * String json = new Gson().toJson(person);
 * // {"name":"John Doe","favoriteFood":null}
 * <pre>
 *
 * @see GsonBuilder#serializeNulls()
 * @see JsonWriter#setSerializeNulls(boolean)
 * @see JsonWriter#forceNullValue()
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface ForceSerializeNull {
  /**
   * Determines when the {@code null} check and the corresponding forced
   * serialization of a JSON {@code null} value happens.
   */
  enum CheckTime {
    /**
     * The {@code null} check is performed before the type adapter for the
     * field is applied. If the field value is {@code null} a JSON {@code null}
     * will be forcefully serialized and the type adapter will not be applied.
     * However, if the field value is non-{@code null} the type adapter is
     * applied but its result (in case it is {@code null}) is not forcefully
     * serialized.
     */
    BEFORE_ADAPTER,
    /**
     * The {@code null} check is performed after the type adapter for the
     * field has been applied, i.e. on the result of the type adapter. Only if
     * the immediately written value is {@code null} it will be forcefully
     * serialized, {@code null} values nested in JSON arrays or objects are
     * serialized as usual.
     */
    AFTER_ADAPTER
  }

  /**
   * Determines at which point to check for a {@code null} field value which should
   * be forcefully serialized.
   *
   * @return when to check for {@code null}.
   */
  CheckTime checkTime() default CheckTime.BEFORE_ADAPTER;
}
